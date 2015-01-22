package com.flozano.statsd.client;

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
import java.util.concurrent.atomic.AtomicInteger;

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

import com.flozano.statsd.client.NettyStatsDClientImpl;
import com.flozano.statsd.test.mockserver.NettyUDPServer;
import com.flozano.statsd.test.mockserver.UDPServer;
import com.flozano.statsd.values.CountValue;
import com.flozano.statsd.values.GaugeValue;
import com.flozano.statsd.values.MetricValue;

@RunWith(Parameterized.class)
public class IntegrationTest {

	static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTest.class);
	static final AtomicInteger PORTS = new AtomicInteger(8125);

	int port;

	@Parameters(name = "{index}: items={0}, rcvbuf={2}, flushProbability={3}, server={1}")
	public static Collection<Object[]> params() {
		List<Object[]> params = new LinkedList<>();
		for (int numberOfItems : Arrays.asList(10, 100, 500)) {
			for (Class<? extends UDPServer> serverClass : Arrays
					.<Class<? extends UDPServer>> asList(
					/* ThreadedUDPServer.class, *//* NettyUDPServer.Nio.class */
					NettyUDPServer.Oio.class)) {
				for (int recvbufValue : Arrays.asList(200_000, 500_000,
						1_000_000)) {
					for (double flushProbability : Arrays.asList(0.0, 0.2, 0.8)) {
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

	private final double flushProbability;

	public IntegrationTest(int numberOfItems,
			Class<? extends UDPServer> serverClass, int recvbufValue,
			double flushProbability) {
		this.numberOfItems = numberOfItems;
		this.serverClass = serverClass;
		this.recvbufValue = recvbufValue;
		this.flushProbability = flushProbability;
	}

	@Before
	public void setUp() {
		LOGGER.info("Starting test {}", name.getMethodName());
		port = PORTS.getAndIncrement();
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
		return new NettyStatsDClientImpl("127.0.0.1", port, flushProbability);
	}

	private UDPServer newServer() {
		try {
			return serverClass.getConstructor(int.class, int.class, int.class)
					.newInstance(port, numberOfItems, recvbufValue);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
