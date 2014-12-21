package com.flozano.statsd.netty;

import static java.util.Objects.requireNonNull;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.net.InetSocketAddress;
import java.util.List;

import com.flozano.statsd.metrics.Metric;

public class UDPEncoder extends MessageToMessageEncoder<Metric> {
	private final InetSocketAddress targetAddress;

	public UDPEncoder(String host, int port) {
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
	protected void encode(ChannelHandlerContext ctx, Metric msg,
			List<Object> out) throws Exception {
		out.add(new DatagramPacket(Unpooled.copiedBuffer(msg.getBytes()),
				targetAddress));
	}
}
