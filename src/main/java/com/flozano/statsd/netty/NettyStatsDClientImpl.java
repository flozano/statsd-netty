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

import java.io.Closeable;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flozano.statsd.StatsDClient;
import com.flozano.statsd.metrics.Metric;

public class NettyStatsDClientImpl implements StatsDClient, Closeable {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(NettyStatsDClientImpl.class);

	private final Bootstrap bootstrap;
	private final Channel channel;

	private final boolean defaultEventLoopGroup;
	private final EventLoopGroup eventLoopGroup;

	private final Timer flushTimer;

	private NettyStatsDClientImpl(String host, int port,
			EventLoopGroup eventLoopGroup, boolean defaultEventLoopGroup,
			int flushProbability) {
		this.eventLoopGroup = requireNonNull(eventLoopGroup);
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
				ch.pipeline()
						// Keep original timer-based approach for now.
						// // .addLast("write-timeouts",
						// // new WriteTimeoutHandler(2, TimeUnit.SECONDS))
						// // .addLast("error-handler",
						// // new WriteTimeoutFlushHandler())
						//
						.addLast(
								"udp",
								new BytesToUDPEncoder(host, port,
										flushProbability))
						.addLast("array-encoder",
								new MetricArrayToBytesEncoder(500))
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

	public NettyStatsDClientImpl(String host, int port,
			EventLoopGroup eventLoopGroup, int flushProbability) {
		this(host, port, eventLoopGroup, false, flushProbability);
	}

	public NettyStatsDClientImpl(String host, int port, int flushProbability) {
		this(host, port, new NioEventLoopGroup(), true, flushProbability);
	}

	@Override
	public CompletableFuture<Void> send(Metric... metrics) {
		validateMetrics(metrics);

		CompletableFuture<Void> cf = new CompletableFuture<>();
		channel.write(metrics).addListener(
				f -> {
					LOGGER.trace("Message sent (future={}, messages={})", f,
							metrics);
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
		return cf;
	}

	@Override
	public void close() throws IOException {
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

	private static void validateMetrics(Metric[] metrics) {
		requireNonNull(metrics);
		if (metrics.length < 1) {
			throw new IllegalArgumentException(
					"At least one metric must be provided");
		}
	}
}
