package org.github.mybridge.netty;

import org.github.mybridge.core.packet.HeaderPacket;
import org.github.mybridge.core.packet.Packet;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

public class DataEncoder extends OneToOneEncoder {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(DataEncoder.class);

	public DataEncoder() {
		logger.debug("DataEncoder...");
		System.out.println("DataEncoder");
	}

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel,
			Object msg) throws Exception {
		logger.debug("encode...");
		byte[] body = (byte[]) msg;
		HeaderPacket header = new HeaderPacket();
		header.setPacketLen(body.length);
		Packet.setPacketId(header.getPacketId());
		ChannelBuffer buffer = ChannelBuffers.buffer(body.length + 4);
		buffer.writeBytes(header.getBytes());
		buffer.writeBytes(body);
		return buffer;
	}
}