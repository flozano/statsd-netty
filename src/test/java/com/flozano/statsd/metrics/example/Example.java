package com.flozano.statsd.metrics.example;

import java.time.Clock;

import org.junit.Test;

import com.flozano.statsd.metrics.Metrics;
import com.flozano.statsd.metrics.MetricsBuilder;
import com.flozano.statsd.metrics.Timer.TimeKeeping;

public class Example {

	@Test
	public void simple() throws Exception {
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

	@Test
	public void batch() throws Exception {
		try (Metrics metrics = MetricsBuilder.create()
				.withClient((clientBuilder) -> //
						clientBuilder.withHost("127.0.0.1") //
								.withPort(8125) //
								.withSampleRate(0.5) // send 50% of metrics only
				).withClock(Clock.systemUTC()).build()) {

			// Send a counter metric immediately
			metrics.counter("visitors").hit();

			// Create a batch of metrics that will be sent at the end of the try
			// block.
			try (Metrics batch = metrics.batch()) {
				batch.gauge("activeDatabaseConnections").value(
						getConnectionsFromPool());
				batch.gauge("activeSessions").delta(-1);
			}

			// Measure the time spent inside the try block
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
