package com.flozano.statsd.client.mock;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ServerHandler.class);

	private final Queue<String> received;

	public ServerHandler(Queue<String> received) {
		this.received = Objects.requireNonNull(received);
	}

	@Override
	public boolean acceptInboundMessage(Object msg) throws Exception {
		return msg != null && msg instanceof DatagramPacket;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg)
			throws Exception {
		LOGGER.trace("Received message: {}", msg);
		received.add(msg.content().toString(StandardCharsets.UTF_8));
	}

}
