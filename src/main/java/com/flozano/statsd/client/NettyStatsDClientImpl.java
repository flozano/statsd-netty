package com.flozano.statsd.client;

import static java.util.Objects.requireNonNull;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.UnaryOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flozano.statsd.values.MetricValue;

final class NettyStatsDClientImpl implements StatsDClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(StatsDClient.class);

	private final Bootstrap bootstrap;
	private final Channel channel;

	private final boolean defaultEventLoopGroup;
	private final EventLoopGroup eventLoopGroup;

	private final Timer flushTimer;

	private final UnaryOperator<MetricValue[]> preprocessor;

	private NettyStatsDClientImpl(UnaryOperator<MetricValue[]> preprocessor, String host, int port,
			EventLoopGroup eventLoopGroup, boolean defaultEventLoopGroup, double flushRate) {
		this.eventLoopGroup = requireNonNull(eventLoopGroup);
		this.preprocessor = requireNonNull(preprocessor);
		this.defaultEventLoopGroup = defaultEventLoopGroup;
		bootstrap = new Bootstrap();
		bootstrap.group(eventLoopGroup);
		bootstrap.channel(NioDatagramChannel.class);
		bootstrap.option(ChannelOption.SO_SNDBUF, 1_000_000);
		bootstrap.option(ChannelOption.SO_RCVBUF, 1_000_000);
		bootstrap.option(ChannelOption.ALLOCATOR, new PooledByteBufAllocator());
		bootstrap.handler(new ChannelInitializer<Channel>() {

			@Override
			protected void initChannel(Channel ch) throws Exception {
				ch.pipeline().addLast("udp", new BytesToUDPEncoder(host, port, flushRate))
						.addLast("array-encoder", new MetricArrayToBytesEncoder(500))
						.addLast("encoder", new MetricToBytesEncoder());
			}
		});
		try {
			this.channel = bootstrap.bind(0).sync().channel();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		flushTimer = new Timer("flush-timer");
		flushTimer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				channel.flush();
				LOGGER.trace("Flushed channel");
			}
		}, 1000, 1000);
	}

	NettyStatsDClientImpl(UnaryOperator<MetricValue[]> processor, String host, int port, EventLoopGroup eventLoopGroup,
			double flushRate) {
		this(processor, host, port, eventLoopGroup, false, flushRate);
	}

	NettyStatsDClientImpl(UnaryOperator<MetricValue[]> processor, String host, int port, double flushRate) {
		this(processor, host, port, new NioEventLoopGroup(), true, flushRate);
	}

	@Override
	public CompletableFuture<Void> send(MetricValue... metricValues) {
		validatemetricValues(metricValues);

		CompletableFuture<Void> cf = new CompletableFuture<>();
		channel.write(metricValues).addListener(f -> {
			LOGGER.trace("Message sent (future={}, metricValues={})", f, metricValues);
			try {
				f.get();
				if (f.isSuccess()) {
					cf.complete(null);
				} else {
					cf.completeExceptionally(new RuntimeException("Future didn't complete successfully"));
				}
			} catch (ExecutionException e) {
				cf.completeExceptionally(e.getCause());
			}
		});
		return cf;
	}

	@Override
	public void close() {
		flushTimer.cancel();
		channel.flush();
		try {
			channel.close().sync();
		} catch (InterruptedException e) {
			LOGGER.warn("Error while closing channel", e);
		} finally {
			if (defaultEventLoopGroup) {
				eventLoopGroup.shutdownGracefully();
			}
		}
	}

	private static void validatemetricValues(MetricValue[] metricValues) {
		requireNonNull(metricValues);
		if (metricValues.length < 1) {
			throw new IllegalArgumentException("At least one metric value must be provided");
		}
	}

	@Override
	public StatsDClient batch() {
		return new BatchStatsDClient(this);
	}
}
