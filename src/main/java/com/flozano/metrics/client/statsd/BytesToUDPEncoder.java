package com.flozano.metrics.client.statsd;

import static java.util.Objects.requireNonNull;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flozano.metrics.client.MetricsClient;

@Sharable
class BytesToUDPEncoder extends MessageToMessageEncoder<ByteBuf> {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(MetricsClient.class);
	private final InetSocketAddress targetAddress;
	private final double flushRate;

	public BytesToUDPEncoder(String host, int port, double flushRate) {
		this.targetAddress = new InetSocketAddress(requireNonNull(host),
				validatePort(port));
		this.flushRate = validateFlushRate(flushRate);
	}

	private static double validateFlushRate(double flushRate) {
		if (flushRate < 0 || flushRate > 1) {
			throw new IllegalArgumentException(
					"Invalid flush rate (must be between 0 and 1)");
		}
		return flushRate;
	}

	private static int validatePort(int port) {
		if (port < 1 || port > 65535) {
			throw new IllegalArgumentException(
					"Bad target port (must be between 1 and 65535)");
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
		if (ThreadLocalRandom.current().nextDouble() < flushRate) {
			ctx.flush();
		}
	}

}
