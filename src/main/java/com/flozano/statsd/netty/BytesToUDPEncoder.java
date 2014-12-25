package com.flozano.statsd.netty;

import static java.util.Objects.requireNonNull;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.net.InetSocketAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BytesToUDPEncoder extends MessageToMessageEncoder<ByteBuf> {
	private static Logger LOGGER = LoggerFactory
			.getLogger(BytesToUDPEncoder.class);
	private final InetSocketAddress targetAddress;

	public BytesToUDPEncoder(String host, int port) {
		this.targetAddress = new InetSocketAddress(requireNonNull(host),
				validatePort(port));
	}

	@Override
	public boolean acceptOutboundMessage(Object msg) throws Exception {
		return msg instanceof ByteBuf;
	}

	private static int validatePort(int port) {
		if (port < 1 || port > 65535) {
			throw new IllegalArgumentException("Bad target port");
		}
		return port;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, ByteBuf msg,
			List<Object> out) throws Exception {
		LOGGER.warn("Writing {} ", msg);
		out.add(new DatagramPacket(msg, targetAddress));
		LOGGER.warn("Wrote {} ", msg);
	}

}
