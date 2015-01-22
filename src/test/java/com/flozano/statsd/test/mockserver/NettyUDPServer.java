package com.flozano.statsd.test.mockserver;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.oio.OioDatagramChannel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class NettyUDPServer implements AutoCloseable, UDPServer {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(NettyUDPServer.class);

	private static final Pattern SPLITTER = Pattern.compile("\n",
			Pattern.LITERAL);

	private final Bootstrap bootstrap;
	private final Channel channel;

	private final EventLoopGroup eventLoopGroup;
	private final Queue<String> queue = new ConcurrentLinkedQueue<>();
	private final Collection<String> items = Collections
			.synchronizedList(new LinkedList<>());
	private final CountDownLatch latch;
	private Timer timer;

	protected abstract EventLoopGroup getEventLoopGroup();

	protected abstract Class<? extends Channel> getChannelClass();

	public NettyUDPServer(int port, int numberOfItems, int recvbufValue) {
		latch = new CountDownLatch(numberOfItems);
		eventLoopGroup = getEventLoopGroup();
		bootstrap = new Bootstrap();
		bootstrap.group(eventLoopGroup);
		// bootstrap.channel(NioDatagramChannel.class);
		bootstrap.channel(getChannelClass());
		bootstrap.option(ChannelOption.SO_RCVBUF, recvbufValue);
		bootstrap.option(ChannelOption.ALLOCATOR, new PooledByteBufAllocator());
		bootstrap.handler(new ChannelInitializer<Channel>() {

			@Override
			protected void initChannel(Channel ch) throws Exception {
				ch.pipeline().addLast("store", new ServerHandler(queue));
			}
		});

		try {
			this.channel = bootstrap.bind(port).sync().channel();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		timer = new Timer();
		configureStatusTimer();
		configurePollingTimer();
	}

	private void configurePollingTimer() {
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				String result;
				do {
					LOGGER.trace("Polling...");
					result = queue.poll();
					if (result != null) {
						SPLITTER.splitAsStream(result).forEach((x) -> {
							items.add(x);
							latch.countDown();
						});
					}
				} while (result != null);
			}
		}, 250, 500);
	}

	private void configureStatusTimer() {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				LOGGER.info("Pending to receive: {}", latch.getCount());
			}
		}, 1000, 1000);
	}

	@Override
	public List<String> getItemsSnapshot() {
		return new ArrayList<>(items);
	}

	@Override
	public void clear() {
		items.clear();
	}

	@Override
	public void close() throws Exception {
		timer.cancel();

		try {
			channel.close().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		eventLoopGroup.shutdownGracefully();
	}

	@Override
	public boolean waitForAllItemsReceived() {
		try {
			return latch.await(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			return false;
		}
	}

	public static class Oio extends NettyUDPServer {

		public Oio(int port, int numberOfItems, int recvbufValue) {
			super(port, numberOfItems, recvbufValue);
		}

		@Override
		protected EventLoopGroup getEventLoopGroup() {
			return new OioEventLoopGroup();
		}

		@Override
		protected Class<? extends Channel> getChannelClass() {
			return OioDatagramChannel.class;
		}

	}

	public static class Nio extends NettyUDPServer {

		public Nio(int port, int numberOfItems, int recvbufValue) {
			super(port, numberOfItems, recvbufValue);
		}

		@Override
		protected EventLoopGroup getEventLoopGroup() {
			return new NioEventLoopGroup();
		}

		@Override
		protected Class<? extends Channel> getChannelClass() {
			return NioDatagramChannel.class;
		}

	}
}
