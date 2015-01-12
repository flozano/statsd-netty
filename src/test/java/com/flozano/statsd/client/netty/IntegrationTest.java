package com.flozano.statsd.client.netty;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flozano.statsd.client.mock.NettyUDPServer;
import com.flozano.statsd.client.mock.UDPServer;
import com.flozano.statsd.client.netty.NettyStatsDClientImpl;
import com.flozano.statsd.metrics.values.CountValue;
import com.flozano.statsd.metrics.values.GaugeValue;
import com.flozano.statsd.metrics.values.MetricValue;

@RunWith(Parameterized.class)
public class IntegrationTest {

	static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTest.class);

	static final int PORT = 8125;

	@Parameters(name = "{index}: items={0}, rcvbuf={2}, flushProbability={3}, server={1}")
	public static Collection<Object[]> params() {
		List<Object[]> params = new LinkedList<>();
		for (int numberOfItems : Arrays.asList(/*10, 100, 500,*/1000)) {
			for (Class<? extends UDPServer> serverClass : Arrays
					.<Class<? extends UDPServer>> asList(
					/* ThreadedUDPServer.class, */NettyUDPServer.Nio.class
					/* ,NettyUDPServer.Oio.class */)) {
				for (int recvbufValue : Arrays.asList(100_000, 500_000,
						1_000_000)) {
					for (int flushProbability : Arrays.asList(0, 20, 80)) {
						params.add(new Object[] { numberOfItems, serverClass,
								recvbufValue, flushProbability });
					}
				}
			}
		}
		return params;
	}

	@Rule
	public TestName name = new TestName();

	private final int numberOfItems;

	private final Class<? extends UDPServer> serverClass;

	private final int recvbufValue;

	private final int flushProbability;

	public IntegrationTest(int numberOfItems,
			Class<? extends UDPServer> serverClass, int recvbufValue,
			int flushProbability) {
		this.numberOfItems = numberOfItems;
		this.serverClass = serverClass;
		this.recvbufValue = recvbufValue;
		this.flushProbability = flushProbability;
	}

	@Before
	public void setUp() {
		LOGGER.info("Starting test {}", name.getMethodName());
	}

	@After
	public void tearDown() {
		LOGGER.info("Finished test {}", name.getMethodName());

	}

	@Test
	public void testManyCalls() throws Exception {
		try (UDPServer server = newServer()) {
			try (NettyStatsDClientImpl c = newClient()) {
				List<CompletableFuture<Void>> css = new ArrayList<>(
						numberOfItems);
				for (int i = 0; i < numberOfItems; i++) {
					css.add(c.send(new CountValue("example", 1)));
				}

				CompletableFuture.allOf(
						css.toArray(new CompletableFuture[numberOfItems])).get(
						10, TimeUnit.SECONDS);
				LOGGER.info("All items sent: {}", numberOfItems);

				assertServer(server, "example:1|c");
			}
		}
	}

	@Test
	public void testSingleCall() throws Exception {
		try (UDPServer server = newServer()) {
			try (NettyStatsDClientImpl c = newClient()) {
				MetricValue[] items = new MetricValue[numberOfItems];
				for (int i = 0; i < numberOfItems; i++) {
					items[i] = new GaugeValue("example", 2);
				}

				c.send(items).get(10, TimeUnit.SECONDS);
				LOGGER.info("All items sent: {}", numberOfItems);

				assertServer(server, "example:2|g");
			}
		}
	}

	private void assertServer(UDPServer server, String expectedValue) {
		assertTrue("All items were not received",
				server.waitForAllItemsReceived());
		LOGGER.info("All items received: {}", numberOfItems);

		assertThat(server.getItemsSnapshot(), everyItem(equalTo(expectedValue)));
		assertEquals(numberOfItems, server.getItemsSnapshot().size());
	}

	private NettyStatsDClientImpl newClient() {
		return new NettyStatsDClientImpl("127.0.0.1", PORT, flushProbability);
	}

	private UDPServer newServer() {
		try {
			return serverClass.getConstructor(int.class, int.class, int.class)
					.newInstance(PORT, numberOfItems, recvbufValue);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
