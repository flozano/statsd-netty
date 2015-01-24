package com.flozano.statsd.metrics;

import java.time.Clock;

import org.junit.Test;
import org.mockito.Mockito;

import com.flozano.statsd.client.StatsDClient;

public class MetricsBuilderTest {
	StatsDClient client = Mockito.mock(StatsDClient.class);

	@Test
	public void testSeveralCombinations() {
		try (Metrics m = MetricsBuilder.create().withClient(client).build()) {
		}
		try (Metrics m = MetricsBuilder.create().withClient(client)
				.withClock(Clock.systemUTC()).build()) {
		}
		try (Metrics m = MetricsBuilder.create().withClient(client)
				.withClock(null).build()) {
		}
		try (Metrics m = MetricsBuilder.create().withClient(client)
				.withPrefix("whatever").build()) {
		}
		try (Metrics m = MetricsBuilder.create().withClient(client)
				.withPrefix(null).build()) {
		}
		try (Metrics m = MetricsBuilder.create().build()) {
		}

	}
}
