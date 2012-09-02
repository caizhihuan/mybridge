package org.github.mybridge.plugin.mina;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;


public class MyProtocalCodecFactory implements ProtocolCodecFactory {
	private final MySQLProtocalEncoder encoder;
	private final MySQLProtocalDecoder decoder;

	public MyProtocalCodecFactory() {
		encoder = new MySQLProtocalEncoder();
		decoder = new MySQLProtocalDecoder();
	}

	public ProtocolEncoder getEncoder(IoSession session) {
		return encoder;
	}

	public ProtocolDecoder getDecoder(IoSession session) {
		return decoder;
	}

}
