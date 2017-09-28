package com.flozano.metrics.client.statsd;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flozano.metrics.client.MetricValue;
import com.flozano.metrics.client.MetricsClient;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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
		write(msg, out);
		LOGGER.trace("Wrote {} ", msg);
	}

	public static String toString(MetricValue msg) {
		ByteBuf buf = Unpooled.buffer(100);
		int length = write(msg, buf);
		return buf.readCharSequence(length, StandardCharsets.UTF_8).toString();
	}

	public static int write(MetricValue msg, ByteBuf out) {
		int start = out.writerIndex();
		out.writeCharSequence(msg.getName(), StandardCharsets.UTF_8);
		out.writeByte(':');
		if (msg.isSignRequiredInValue() && msg.getValue() > 0) {
			out.writeByte('+');
		}
		out.writeCharSequence(Long.toString(msg.getValue()), StandardCharsets.US_ASCII);
		out.writeByte('|');
		out.writeCharSequence(msg.getCode(), StandardCharsets.US_ASCII);
		Double r = msg.getSampleRate();
		if (r != null) {
			out.writeCharSequence("|@", StandardCharsets.US_ASCII);
			out.writeCharSequence(String.format("%1.2f", r), StandardCharsets.US_ASCII);
		}
		return out.writerIndex() - start;
	}
}
