package com.flozano.statsd.client;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flozano.statsd.values.MetricValue;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

@Sharable
class MetricToBytesEncoder extends MessageToByteEncoder<MetricValue> {

	private static Logger LOGGER = LoggerFactory.getLogger(StatsDClient.class);


	@Override
	public boolean acceptOutboundMessage(Object msg) throws Exception {
		return msg instanceof MetricValue;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, MetricValue msg,
			ByteBuf out) throws Exception {
		LOGGER.trace("Writing {} ", msg);
		msg.toStringParts((part) -> out.writeBytes(part
				.getBytes(StandardCharsets.UTF_8)));
		LOGGER.trace("Wrote {} ", msg);
	}

}
