package com.flozano.statsd.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flozano.statsd.metrics.Metric;

public class MetricToBytesEncoder extends MessageToMessageEncoder<Metric> {

	private static Logger LOGGER = LoggerFactory.getLogger(MetricToBytesEncoder.class);

	@Override
	public boolean acceptOutboundMessage(Object msg) throws Exception {
		return msg instanceof Metric;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Metric msg,
			List<Object> out) throws Exception {
		LOGGER.warn("Writing {} ", msg);

		ByteBuf buf = ctx.alloc().buffer();
		msg.toStringParts((part) -> buf.writeBytes(part
				.getBytes(StandardCharsets.UTF_8)));
		out.add(buf);
		LOGGER.warn("Wrote {} ", msg);
	}

}
