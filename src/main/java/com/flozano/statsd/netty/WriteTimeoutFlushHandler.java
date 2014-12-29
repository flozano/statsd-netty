package com.flozano.statsd.netty;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.WriteTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WriteTimeoutFlushHandler extends ChannelDuplexHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WriteTimeoutFlushHandler.class);

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
