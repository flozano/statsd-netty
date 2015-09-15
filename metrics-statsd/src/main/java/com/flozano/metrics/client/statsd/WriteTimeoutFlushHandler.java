package com.flozano.metrics.client.statsd;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.WriteTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flozano.metrics.client.MetricsClient;

@Sharable
class WriteTimeoutFlushHandler extends ChannelDuplexHandler {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(MetricsClient.class);

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		if (cause instanceof WriteTimeoutException) {
			LOGGER.debug("Timeout flush");
			ctx.flush();
		} else {
			super.exceptionCaught(ctx, cause);
		}
	}
}
