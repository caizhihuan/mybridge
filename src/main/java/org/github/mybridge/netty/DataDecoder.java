package org.github.mybridge.netty;

import org.github.mybridge.core.packet.HeaderPacket;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

public class DataDecoder extends FrameDecoder {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(DataDecoder.class);
	static int READ_HEADER = 0;//
	static int READ_BODY = 1;//
	int currentState = READ_HEADER;

	public DataDecoder() {
		System.out.println(this.getClass().getName());
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel,
			ChannelBuffer buffer) throws Exception {
		logger.debug("decode...");
		if (buffer.readableBytes() < 5) {
			return null;
		} else {
			byte[] header = new byte[4];
			buffer.getBytes(0, header);
			HeaderPacket headerPacket = new HeaderPacket();
			headerPacket.putBytes(header);
			buffer.skipBytes(4);
			return buffer;
		}
	}
}
