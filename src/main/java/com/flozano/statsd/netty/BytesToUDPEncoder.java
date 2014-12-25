package com.flozano.statsd.netty;

import static java.util.Objects.requireNonNull;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BytesToUDPEncoder extends MessageToMessageEncoder<ByteBuf> {
	private static Logger LOGGER = Logger.getLogger(BytesToUDPEncoder.class
			.getName());
	private final InetSocketAddress targetAddress;

	public BytesToUDPEncoder(String host, int port) {
		this.targetAddress = new InetSocketAddress(requireNonNull(host),
				validatePort(port));
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
		LOGGER.log(Level.WARNING, "Writing {} ", msg);
		out.add(new DatagramPacket(msg, targetAddress));
		LOGGER.log(Level.WARNING, "Wrote {} ", msg);
	}

}
