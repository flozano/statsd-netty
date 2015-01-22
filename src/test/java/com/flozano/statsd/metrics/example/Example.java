package com.flozano.statsd.metrics.example;

import java.time.Clock;

import org.junit.Test;

import com.flozano.statsd.metrics.Metrics;
import com.flozano.statsd.metrics.MetricsBuilder;
import com.flozano.statsd.metrics.Timer.TimeKeeping;

public class Example {

	@Test
	public void withBuilder() throws Exception {
		try (Metrics metrics = MetricsBuilder.create()
				.withClient((clientBuilder) -> //
						clientBuilder.withHost("127.0.0.1") //
								.withPort(8125) //
								.withSampleRate(0.5) //
				).withClock(Clock.systemUTC()).build()) {
			metrics.counter("visitors").hit();
			metrics.counter("soldItems").count(25);
			metrics.gauge("activeDatabaseConnections").value(
					getConnectionsFromPool());
			metrics.gauge("activeSessions").delta(-1);

			try (TimeKeeping o = metrics.timer("timeSpentSavingData").time()) {
				saveData();
			}
		}
	}

	private static long getConnectionsFromPool() {
		return 10;
	}

	private static void saveData() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
