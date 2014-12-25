package com.flozano.statsd.netty;

import static com.jayway.awaitility.Awaitility.await;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.flozano.statsd.metrics.Count;
import com.flozano.statsd.mock.DummyUDPServer;

public class IntegrationTest {

	static int NUMBER_OF_ITEMS = 25;

	@Test
	public void test() throws Exception {
		try (DummyUDPServer server = new DummyUDPServer(8125)) {
			try (NettyStatsDClientImpl c = new NettyStatsDClientImpl(
					"127.0.0.1", 8125)) {
				for (int i = 0; i < NUMBER_OF_ITEMS; i++) {
					c.send(new Count("example", 1));
				}
				await().atMost(5, TimeUnit.SECONDS)
						.until(() -> server.getItemsSnapshot().size() == NUMBER_OF_ITEMS);
			}
		}
	}
}
