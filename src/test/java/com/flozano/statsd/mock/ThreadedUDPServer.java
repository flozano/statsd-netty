package com.flozano.statsd.mock;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadedUDPServer extends Thread implements UDPServer {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ThreadedUDPServer.class);

	private final List<String> items = new CopyOnWriteArrayList<>();
	private final DatagramSocket socket;

	private final AtomicBoolean stopped = new AtomicBoolean(false);
	private final CountDownLatch latch;
	private final Timer timer;

	public ThreadedUDPServer(int port, int numberOfItems) {
		super("threaded-udp-server");
		latch = new CountDownLatch(numberOfItems);

		try {
			socket = new DatagramSocket(port);
			socket.setSoTimeout(5000);
			socket.setReceiveBufferSize(10 * 1024 * 1204);
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				LOGGER.info("Pending to receive: {}", latch.getCount());
			}
		}, 1000, 1000);
		start();
	}

	@Override
	public void run() {

		while (!stopped.get()) {
			byte[] buf = new byte[512];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			try {
				socket.receive(packet);
				items.add(new String(packet.getData(), StandardCharsets.UTF_8)
						.trim());
				latch.countDown();
			} catch (SocketTimeoutException e) {
				// timeout
			} catch (IOException e) {
				if (stopped.get()) {

				} else {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public List<String> getItemsSnapshot() {
		return new ArrayList<String>(items);
	}

	@Override
	public void clear() {
		items.clear();
	}

	@Override
	public void close() throws Exception {
		stopped.set(true);
		interrupt();
		socket.close();
		timer.cancel();
	}

	@Override
	public void waitForAllItemsReceived() throws InterruptedException {
		latch.await(2, TimeUnit.MINUTES);
	}
}
