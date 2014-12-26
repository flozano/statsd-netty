package com.flozano.statsd.mock;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerHandler extends SimpleChannelInboundHandler<String> {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ServerHandler.class);

	private final Collection<String> received;

	private CountDownLatch latch;

	public ServerHandler(Collection<String> received, CountDownLatch latch) {
		this.received = Objects.requireNonNull(received);
		this.latch = Objects.requireNonNull(latch);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg)
			throws Exception {
		LOGGER.info("Received message: {}", msg);
		received.add(msg);
		latch.countDown();
	}

}
