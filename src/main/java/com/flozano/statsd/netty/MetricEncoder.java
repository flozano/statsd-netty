package com.flozano.statsd.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.flozano.statsd.metrics.Metric;

public class MetricEncoder extends MessageToByteEncoder<Metric> {

	private static Logger LOGGER = Logger.getLogger(MetricEncoder.class
			.getName());

	@Override
	public boolean acceptOutboundMessage(Object msg) throws Exception {
		return msg instanceof Metric;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Metric msg, ByteBuf out)
			throws Exception {
		LOGGER.log(Level.FINE, "Writing {} ", msg);
		msg.toStringParts((part) -> out.writeBytes(part
				.getBytes(StandardCharsets.UTF_8)));
		LOGGER.log(Level.FINE, "Wrote {} ", msg);
	}

}
