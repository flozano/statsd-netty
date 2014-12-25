package com.flozano.statsd.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;

import com.flozano.statsd.metrics.Metric;

public class MetricEncoder extends MessageToByteEncoder<Metric> {

	@Override
	protected void encode(ChannelHandlerContext ctx, Metric msg, ByteBuf out)
			throws Exception {
		msg.toStringParts((part) -> out.writeBytes(part
				.getBytes(StandardCharsets.UTF_8)));
	}

}
