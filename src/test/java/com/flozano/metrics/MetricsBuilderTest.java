package com.flozano.metrics;

import java.time.Clock;

import org.junit.Test;
import org.mockito.Mockito;

import com.flozano.metrics.Metrics;
import com.flozano.metrics.MetricsBuilder;
import com.flozano.metrics.client.MetricsClient;

public class MetricsBuilderTest {
	MetricsClient client = Mockito.mock(MetricsClient.class);

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
