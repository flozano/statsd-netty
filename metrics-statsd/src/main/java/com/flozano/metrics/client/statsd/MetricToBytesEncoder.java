package com.flozano.metrics.client.statsd;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flozano.metrics.Tags;
import com.flozano.metrics.client.MetricValue;
import com.flozano.metrics.client.MetricsClient;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

@Sharable
class MetricToBytesEncoder extends MessageToByteEncoder<MetricValue> {

	private static Logger LOGGER = LoggerFactory.getLogger(MetricsClient.class);

	@Override
	public boolean acceptOutboundMessage(Object msg) throws Exception {
		return msg instanceof MetricValue;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, MetricValue msg, ByteBuf out) throws Exception {
		LOGGER.trace("Writing {} ", msg);
		toStringParts(msg, (part) -> out.writeBytes(part.getBytes(StandardCharsets.UTF_8)));
		LOGGER.trace("Wrote {} ", msg);
	}

	public static String toString(MetricValue msg) {
		StringBuilder sb = new StringBuilder();
		toStringParts(msg, sb::append);
		return sb.toString();
	}

	public static void toStringParts(MetricValue msg, Consumer<String> parts) {
		parts.accept(msg.getName());
		parts.accept(":");
		if (msg.isSignRequiredInValue() && msg.getValue() > 0) {
			parts.accept("+");
		}
		parts.accept(Long.toString(msg.getValue()));
		parts.accept("|");
		parts.accept(msg.getCode());
		Double r = msg.getSampleRate();
		if (r != null) {
			parts.accept("|@");
			parts.accept(String.format("%1.2f", r));
		}

		Tags tags = msg.getTags();
		if (!tags.isEmpty()) {
		  parts.accept("|#");
			parts.accept(tags
					.stream()
					.sorted()
					.map(tag -> String.valueOf(tag.name) + ':' + tag.value)
					.collect(Collectors.joining(","))
			);
    }
	}
}
