package com.flozano.statsd.netty;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flozano.statsd.metrics.Count;
import com.flozano.statsd.metrics.Gauge;
import com.flozano.statsd.metrics.Metric;
import com.flozano.statsd.mock.ThreadedUDPServer;
import com.flozano.statsd.mock.UDPServer;

@RunWith(Parameterized.class)
public class IntegrationTest {

	static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTest.class);

	static final int PORT = 8125;

	@Parameters
	public static Collection<Object[]> params() {
		return Arrays.asList(new Object[] { 1 }, new Object[] { 10 },
				new Object[] { 100 }, new Object[] { 1000 });
	}

	private final int numberOfItems;

	public IntegrationTest(int numberOfItems) {
		this.numberOfItems = numberOfItems;
	}

	@Test
	public void testManyCalls() throws Exception {
		try (UDPServer server = newServer(numberOfItems)) {
			try (NettyStatsDClientImpl c = newClient()) {
				List<CompletableFuture<Void>> css = new ArrayList<>(
						numberOfItems);
				for (int i = 0; i < numberOfItems; i++) {
					css.add(c.send(new Count("example", 1)));
				}
				
				CompletableFuture.allOf(
						css.toArray(new CompletableFuture[numberOfItems])).get(
						2, TimeUnit.MINUTES);
				LOGGER.info("All items sent: {}", numberOfItems);
				
				server.waitForAllItemsReceived();
				LOGGER.info("All items received: {}", numberOfItems);

				assertThat(server.getItemsSnapshot(),
						everyItem(equalTo("example:1|c")));
				assertEquals(numberOfItems, server.getItemsSnapshot().size());
			}
		}
	}

	@Test
	public void testSingleCall() throws Exception {
		try (UDPServer server = newServer(numberOfItems)) {
			try (NettyStatsDClientImpl c = newClient()) {
				Metric[] items = new Metric[numberOfItems];
				for (int i = 0; i < numberOfItems; i++) {
					items[i] = new Gauge("example", 2);
				}

				c.send(items).get(2, TimeUnit.MINUTES);
				LOGGER.info("All items sent: {}", numberOfItems);

				server.waitForAllItemsReceived();
				LOGGER.info("All items received: {}", numberOfItems);

				assertThat(server.getItemsSnapshot(),
						everyItem(equalTo("example:2|g")));
				assertEquals(numberOfItems, server.getItemsSnapshot().size());
			}
		}
	}

	private NettyStatsDClientImpl newClient() {
		return new NettyStatsDClientImpl("127.0.0.1", PORT);
	}

	private UDPServer newServer(int numberOfItems) {
		// return new DummyUDPServer(PORT, numberOfItems);
		return new ThreadedUDPServer(PORT, numberOfItems);
	}
}
