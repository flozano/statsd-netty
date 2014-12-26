package com.flozano.statsd;

import java.util.concurrent.CompletableFuture;

import com.flozano.statsd.metrics.Metric;

public interface StatsDClient {

	/**
	 * Send a bunch of metrics to StatsD server.
	 * 
	 * @param metrics
	 * @return
	 */
	CompletableFuture<Void> send(Metric... metrics);

}
