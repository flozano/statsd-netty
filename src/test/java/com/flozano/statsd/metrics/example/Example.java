package com.flozano.statsd.metrics.example;

import com.flozano.statsd.client.netty.NettyStatsDClientImpl;
import com.flozano.statsd.metrics.Metrics;
import com.flozano.statsd.metrics.Timer.Ongoing;

public class Example {
	public void simple() {
		// Indicates how likely the writes will flush to the statsd server
		int rateOfFlush = 80;

		// Metrics class allows auto-closing of resources
		try (Metrics metrics = new Metrics(new NettyStatsDClientImpl(
				"127.0.0.1", 8125, rateOfFlush))) {
			metrics.counter("visitors").hit();
			metrics.counter("soldItems").count(25);
			metrics.gauge("activeDatabaseConnections").value(
					getConnectionsFromPool());
			metrics.gauge("activeSessions").delta(-1);
			try (Ongoing o = metrics.timer("timeSpentSavingData").time()) {
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
