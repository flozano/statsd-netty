package com.flozano.statsd.mock;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.HashedWheelTimer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyUDPServer implements AutoCloseable {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(DummyUDPServer.class);

	private final Bootstrap bootstrap;
	private final Channel channel;

	private final EventLoopGroup eventLoopGroup;
	private final CopyOnWriteArrayList<String> items = new CopyOnWriteArrayList<>();
	private final CountDownLatch latch;
	private HashedWheelTimer timer;

	public DummyUDPServer(int port, int numberOfItems) {
		latch = new CountDownLatch(numberOfItems);
		eventLoopGroup = new NioEventLoopGroup(1);
		bootstrap = new Bootstrap();
		bootstrap.group(eventLoopGroup);
		bootstrap.channel(NioDatagramChannel.class);
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
		timer = new HashedWheelTimer();
		timer.newTimeout(
				timeout -> LOGGER.info("Pending to receive: {}",
						latch.getCount()), 2, TimeUnit.SECONDS);
	}

	public List<String> getItemsSnapshot() {
		return new ArrayList<>(items);
	}

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
		timer.stop();
	}

	public void waitForAllItemsReceived() throws InterruptedException {
		latch.await(2, TimeUnit.MINUTES);
	}
}
