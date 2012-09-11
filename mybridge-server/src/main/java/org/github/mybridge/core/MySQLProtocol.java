package org.github.mybridge.core;

import java.util.ArrayList;
import java.util.List;

import org.github.mybridge.core.handler.Handler;
import org.github.mybridge.core.handler.MysqlCommondHandler;
import org.github.mybridge.core.handler.exception.CommandException;
import org.github.mybridge.core.packet.AuthenticationPacket;
import org.github.mybridge.core.packet.CommandPacket;
import org.github.mybridge.core.packet.ErrorPacket;
import org.github.mybridge.core.packet.HandshakeState;
import org.github.mybridge.core.packet.InitialHandshakePacket;
import org.github.mybridge.core.packet.OkPacket;
import org.github.mybridge.core.packet.Packet;
import org.github.mybridge.utils.StringUtils;
import org.jboss.netty.channel.Channel;

public class MySQLProtocol {
	private final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(this
			.getClass());
	private List<Packet> requests = null;
	private HandshakeState state;
	private Handler handler;

	public MySQLProtocol() {
		requests = new ArrayList<Packet>();
		handler = new MysqlCommondHandler();
	}

	public void onConnected(Channel channel) {
		state = HandshakeState.WRITE_INIT;
		InitialHandshakePacket initPacket = new InitialHandshakePacket();
		channel.write(initPacket.getBytes());
	}

	public void writeComplete() {
		switch (state) {
		case WRITE_INIT:
			state = HandshakeState.READ_AUTH;
			break;
		case WRITE_RESULT:
			state = HandshakeState.READ_COMMOND;
		case CLOSE:
		default:
			break;
		}
	}

	public void onRequestReceived(Channel channel, byte[] bytes) {
		String msg = "";
		ErrorPacket errPacket = null;
		switch (state) {
		case READ_AUTH:
			LOG.debug("READ_AUTH");
			LOG.debug(StringUtils.printHex(bytes));
			state = HandshakeState.WRITE_RESULT;
			AuthenticationPacket auth = new AuthenticationPacket();
			auth.putBytes(bytes);
			
			String user = "";
			if (auth.clientUser.length() > 1) {
				user = auth.clientUser.substring(0,
						auth.clientUser.length() - 1);
			}
			try {
				// 编码
				if (MySQLCommand.index2Charset
						.containsKey((int) auth.charsetNum)) {
					handler.setCharset(MySQLCommand.index2Charset
							.get((int) auth.charsetNum));
				}
				// 取得schema
				if (auth.dbName.length() > 0) {
					String dbname = auth.dbName.substring(0,
							auth.dbName.length() - 1);
					handler.setDb(dbname);
				}
				// 验证用户名与密码

				if (auth.checkAuth(user, auth.clientPassword)) {
					OkPacket ok = new OkPacket();
					channel.write(ok.getBytes());
					break;
				}
			} catch (Exception e) {
				LOG.debug("packet auth failed  " + e);
				msg = "handshake authpacket failed  ";
				errPacket = new ErrorPacket(1045, msg);
				channel.write(errPacket.getBytes());
				state = HandshakeState.CLOSE;
				break;
			}
			msg = "Access denied for user " + auth.clientUser;
			errPacket = new ErrorPacket(1045, msg);
			channel.write(errPacket.getBytes());
			state = HandshakeState.CLOSE;
			break;
		case READ_COMMOND:
			LOG.debug("READ_COMMOND");
			state = HandshakeState.WRITE_RESULT;
			CommandPacket cmd = new CommandPacket();
			cmd.putBytes(bytes);
			List<Packet> resultlist = null;
			try {
				resultlist = handler.executeCommand(cmd);
			} catch (CommandException e) {
				e.printStackTrace();
				errPacket = new ErrorPacket(1046, "server error");
				channel.write(errPacket);
			}
			if (resultlist != null && resultlist.size() > 0) {
				writePacketList(channel, resultlist);
			}
			break;
		default:
			break;
		}
	}

	private void writePacketList(Channel channel, List<Packet> resultlist) {
		for (Packet packet : resultlist) {
			byte[] temp = packet.getBytes();
			channel.write(temp);
		}
	}
}
