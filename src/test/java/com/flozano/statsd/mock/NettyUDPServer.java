package com.flozano.statsd.mock;

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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class NettyUDPServer implements AutoCloseable, UDPServer {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(NettyUDPServer.class);

	private final Bootstrap bootstrap;
	private final Channel channel;

	private final EventLoopGroup eventLoopGroup;
	private final Collection<String> items = new CopyOnWriteArrayList<>();
	private final CountDownLatch latch;
	private Timer timer;

	protected abstract EventLoopGroup getEventLoopGroup();

	protected abstract Class<? extends Channel> getChannelClass();

	public NettyUDPServer(int port, int numberOfItems) {
		latch = new CountDownLatch(numberOfItems);
		eventLoopGroup = getEventLoopGroup();
		bootstrap = new Bootstrap();
		bootstrap.group(eventLoopGroup);
		// bootstrap.channel(NioDatagramChannel.class);
		bootstrap.channel(getChannelClass());
		bootstrap.option(ChannelOption.SO_RCVBUF, 1024 * 1024 * 5);
		bootstrap.option(ChannelOption.ALLOCATOR, new PooledByteBufAllocator());
		bootstrap.handler(new ChannelInitializer<Channel>() {

			@Override
			protected void initChannel(Channel ch) throws Exception {
				ch.pipeline().addLast("udp", new UDPToStringDecoder())
						.addLast("store", new ServerHandler(items, latch));
			}
		});
		try {
			this.channel = bootstrap.bind(port).sync().channel();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		timer = new Timer();
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
		try {
			channel.close().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		eventLoopGroup.shutdownGracefully();
		timer.cancel();
	}

	@Override
	public boolean waitForAllItemsReceived() throws InterruptedException {
		return latch.await(10, TimeUnit.SECONDS);
	}

	public static class Oio extends NettyUDPServer {

		public Oio(int port, int numberOfItems) {
			super(port, numberOfItems);
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

		public Nio(int port, int numberOfItems) {
			super(port, numberOfItems);
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
