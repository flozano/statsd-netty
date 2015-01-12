package com.flozano.statsd.client.netty;

import static java.util.Objects.requireNonNull;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BytesToUDPEncoder extends MessageToMessageEncoder<ByteBuf> {
	private static Logger LOGGER = LoggerFactory
			.getLogger(BytesToUDPEncoder.class);
	private final InetSocketAddress targetAddress;
	private final int flushPercent;

	public BytesToUDPEncoder(String host, int port, int flushProbability) {
		this.targetAddress = new InetSocketAddress(requireNonNull(host),
				validatePort(port));
		this.flushPercent = validateFlushProbability(flushProbability);
	}

	private static int validateFlushProbability(int flushProbability) {
		if (flushProbability < 0 || flushProbability > 100) {
			throw new IllegalArgumentException(
					"Invalid percentage in flush probability");
		}
		return flushProbability;
	}

	private static int validatePort(int port) {
		if (port < 1 || port > 65535) {
			throw new IllegalArgumentException("Bad target port");
		}
		return port;
	}

	@Override
	public boolean acceptOutboundMessage(Object msg) throws Exception {
		return msg instanceof ByteBuf;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, ByteBuf msg,
			List<Object> out) throws Exception {
		LOGGER.trace("Writing {} ", msg);
		msg.retain(); // Retain because reuse of same byteBuf?
		out.add(new DatagramPacket(msg, targetAddress));
		LOGGER.trace("Wrote {} ", msg);
		if (ThreadLocalRandom.current().nextInt(100) < flushPercent) {
			ctx.flush();
		}
	}

}
