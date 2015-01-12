package com.flozano.statsd.client;

import java.util.concurrent.CompletableFuture;

import com.flozano.statsd.metrics.values.MetricValue;

public interface StatsDClient {

	/**
	 * Send a bunch of metrics to StatsD server.
	 * 
	 * @param metrics
	 *            the metrics to be sent
	 * @return a future that allows to hook on the operation completion
	 */
	CompletableFuture<Void> send(MetricValue... metrics);

}
