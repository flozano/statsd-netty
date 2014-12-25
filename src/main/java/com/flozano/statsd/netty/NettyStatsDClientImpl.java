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
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.flozano.statsd.StatsDClient;
import com.flozano.statsd.metrics.Metric;

public class NettyStatsDClientImpl implements StatsDClient, Closeable {

	private static final Logger LOGGER = Logger
			.getLogger(NettyStatsDClientImpl.class.getName());

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
		bootstrap.option(ChannelOption.ALLOCATOR, new PooledByteBufAllocator());
		bootstrap.handler(new ChannelInitializer<Channel>() {

			@Override
			protected void initChannel(Channel ch) throws Exception {
				ch.pipeline().addLast("encoder", new MetricEncoder());
				ch.pipeline().addLast("udp", new BytesToUDPEncoder(host, port));
				// ch.pipeline().addLast("udp", new UDPEncoder(host, port));
				ch.pipeline().addLast(
						"logging-udp",
						new LoggingHandler(NettyStatsDClientImpl.class,
								LogLevel.TRACE));
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
	public void send(Metric... metrics) {
		for (Metric m : metrics) {
			channel.write(m).addListener(
					f -> {
						LOGGER.log(Level.FINE, "Message sent {1}: {0}",
								new Object[] { m, f.isDone() });
					});
		}
	}

	@Override
	public void close() throws IOException {
		flushTimer.stop();
		channel.flush();
		if (defaultEventLoopGroup) {
			eventLoopGroup.shutdownGracefully();
		}
	}
}
