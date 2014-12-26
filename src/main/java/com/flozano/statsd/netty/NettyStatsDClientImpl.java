package com.flozano.statsd.netty;

import static java.util.Objects.requireNonNull;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flozano.statsd.StatsDClient;
import com.flozano.statsd.metrics.Metric;

public class NettyStatsDClientImpl implements StatsDClient, Closeable {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(NettyStatsDClientImpl.class);

	private final Bootstrap bootstrap;
	private final Channel channel;
	private final Timer flushTimer;

	private final boolean defaultEventLoopGroup;
	private final EventLoopGroup eventLoopGroup;

	private NettyStatsDClientImpl(String host, int port,
			EventLoopGroup eventLoopGroup, boolean defaultEventLoopGroup) {
		this.eventLoopGroup = requireNonNull(eventLoopGroup);
		this.defaultEventLoopGroup = defaultEventLoopGroup;
		bootstrap = new Bootstrap();
		bootstrap.group(eventLoopGroup);
		bootstrap.channel(NioDatagramChannel.class);
		bootstrap.option(ChannelOption.SO_SNDBUF, 5 * 1024 * 1024);
		bootstrap.option(ChannelOption.ALLOCATOR, new PooledByteBufAllocator());
		bootstrap.handler(new ChannelInitializer<Channel>() {

			@Override
			protected void initChannel(Channel ch) throws Exception {
				ch.pipeline().addLast("udp", new BytesToUDPEncoder(host, port))
						.addLast("encoder", new MetricToBytesEncoder());
			}
		});
		try {
			this.channel = bootstrap.bind(0).sync().channel();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		flushTimer = new HashedWheelTimer();
		flushTimer.newTimeout(timeout -> channel.flush(), 1, TimeUnit.SECONDS);
	}

	public NettyStatsDClientImpl(String host, int port,
			EventLoopGroup eventLoopGroup) {
		this(host, port, eventLoopGroup, false);
	}

	public NettyStatsDClientImpl(String host, int port) {
		this(host, port, new NioEventLoopGroup(), true);
	}

	@Override
	public CompletableFuture<Void> send(Metric... metrics) {
		validateMetrics(metrics);
		ArrayList<CompletableFuture<Void>> cfs = new ArrayList<CompletableFuture<Void>>(
				metrics.length);
		for (Metric m : metrics) {
			CompletableFuture<Void> cf = new CompletableFuture<>();
			channel.write(m)
					.addListener(
							f -> {
								LOGGER.trace(
										"Message sent (future={}, message={})",
										f, m);
								try {
									f.get();
									if (f.isSuccess()) {
										cf.complete(null);
									} else {
										cf.completeExceptionally(new RuntimeException(
												"Future didn't complete successfully"));
									}
								} catch (ExecutionException e) {
									cf.completeExceptionally(e.getCause());
								}
							});
			cfs.add(cf);
		}
		return CompletableFuture.allOf(cfs.toArray(new CompletableFuture[cfs
				.size()]));
	}

	@Override
	public void close() throws IOException {
		flushTimer.stop();
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

	private static void validateMetrics(Metric[] metrics) {
		requireNonNull(metrics);
		if (metrics.length < 1) {
			throw new IllegalArgumentException(
					"At least one metric must be provided");
		}
	}
}
