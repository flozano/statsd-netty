package com.flozano.metrics.example;

import java.time.Clock;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.flozano.metrics.Metrics;
import com.flozano.metrics.MetricsBuilder;
import com.flozano.metrics.Timer.TimeKeeping;
import com.flozano.metrics.client.statsd.StatsDMetricsClientBuilder;

public class Example {

	@Test
	public void simple() throws Exception {
		try (Metrics metrics = MetricsBuilder.create()
				.withClient(StatsDMetricsClientBuilder.create().withHost("127.0.0.1") //
						.withPort(8125) //
						.withSampleRate(0.5) //
						.build())
				.withClock(Clock.systemUTC()).build()) {
			metrics.counter("visitors").hit();
			metrics.counter("soldItems").count(25);
			metrics.gauge("activeDatabaseConnections").value(getConnectionsFromPool());
			metrics.gauge("activeSessions").delta(-1);

			try (TimeKeeping o = metrics.timer("timeSpentSavingData").time()) {
				saveData();
			}
		}
	}

	@Test
	public void batch() throws Exception {
		try (Metrics metrics = MetricsBuilder.create()
				.withClient(StatsDMetricsClientBuilder.create().withHost("127.0.0.1") //
						.withPort(8125) //
						.withSampleRate(0.5) // send 50% of metrics only
						.build())
				.withClock(Clock.systemUTC()).build()) {

			// Send a counter metric immediately
			metrics.counter("visitors").hit();

			// Create a batch of metrics that will be sent at the end of the try
			// block.
			try (Metrics batch = metrics.batch()) {
				batch.gauge("activeDatabaseConnections").value(getConnectionsFromPool());
				batch.gauge("activeSessions").delta(-1);
			}

			// Schedule a couple of gauges to be reported every 60 seconds
			metrics.gauge("databaseConnectionPool", "activeConnections").supply(60, TimeUnit.SECONDS,
					() -> getConnectionsFromPool());

			metrics.gauge("databaseConnectionPool", "waitingForConnection").supply(1, TimeUnit.MINUTES,
					() -> getWaitingForConnection());

			// Measure the time spent inside the try block
			try (TimeKeeping o = metrics.timer("timeSpentSavingData").time()) {
				saveData();
			}
		}
	}

	private Long getWaitingForConnection() {
		// TODO Auto-generated method stub
		return null;
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
