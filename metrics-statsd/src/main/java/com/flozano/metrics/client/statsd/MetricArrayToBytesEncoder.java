package com.flozano.metrics.client.statsd;

import static java.util.Objects.requireNonNull;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flozano.metrics.client.MetricValue;
import com.flozano.metrics.client.MetricsClient;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

@Sharable
class MetricArrayToBytesEncoder extends MessageToMessageEncoder<MetricValue[]> {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(MetricsClient.class);

	public static final int MAX_MTU = 800;

	public static final byte[] SEPARATOR = "\n"
			.getBytes(StandardCharsets.UTF_8);

	public final int maxBytesPerPacket;

	public MetricArrayToBytesEncoder(int maxBytesPerPacket) {
		this.maxBytesPerPacket = validateMaxBytes(maxBytesPerPacket);
	}

	private static int validateMaxBytes(int maxBytesPerPacket) {
		if (maxBytesPerPacket < 0 || maxBytesPerPacket > MAX_MTU) {
			throw new IllegalArgumentException(
					"maxBytesPerPacket must be between 0 and " + MAX_MTU);
		}
		return maxBytesPerPacket;
	}

	@Override
	public boolean acceptOutboundMessage(Object msg) throws Exception {
		return msg != null && msg instanceof MetricValue[];
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, MetricValue[] msg,
			List<Object> out) throws Exception {
		if (msg == null || msg.length == 0) {
			return;
		}

		ByteBuf buf = null;
		int currentBytes = 0;
		for (MetricValue m : msg) {
			if (m == null) {
				continue;
			}

			if (currentBytes >= maxBytesPerPacket) {
				currentBytes = 0;
				out.add(buf);
				// TODO avoid "last-message allocation"
				buf = ctx.alloc().buffer();
			} else if (currentBytes > 0) {
				buf.writeBytes(SEPARATOR);
				currentBytes += SEPARATOR.length;
			} else if (currentBytes == 0) {
				buf = ctx.alloc().buffer();
			} else {
				assert false;
			}

			ByteCountingWriter writer = new ByteCountingWriter(buf);
			MetricToBytesEncoder.toStringParts(m, writer);
			currentBytes += writer.getWrittenBytes();
		}

		if (buf != null) {
			out.add(buf);
		}
		LOGGER.trace("Encoded {} metrics into {} different packets",
				msg.length, out.size());
	}

	private static class ByteCountingWriter implements Consumer<String> {

		private final ByteBuf outputBuffer;
		private int writtenBytes = 0;

		public ByteCountingWriter(ByteBuf outputBuffer) {
			this.outputBuffer = requireNonNull(outputBuffer);
		}

		@Override
		public void accept(String t) {
			byte[] partBytes = t.getBytes(StandardCharsets.UTF_8);
			outputBuffer.writeBytes(partBytes);
			writtenBytes += partBytes.length;
		}

		public int getWrittenBytes() {
			return writtenBytes;
		}

	}
}
