package com.flozano.statsd.mock;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerHandler extends SimpleChannelInboundHandler<String> {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ServerHandler.class);

	private final List<String> received;

	public ServerHandler(List<String> received) {
		this.received = received;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg)
			throws Exception {
		LOGGER.info("Received message: {}", msg);
		received.add(msg);
	}

}
