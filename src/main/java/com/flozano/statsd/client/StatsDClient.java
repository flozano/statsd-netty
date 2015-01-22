package com.flozano.statsd.client;

import java.util.concurrent.CompletableFuture;

import com.flozano.statsd.values.MetricValue;

/**
 * Client for StatsD
 *
 * @author flozano
 *
 */
public interface StatsDClient extends AutoCloseable {

	/**
	 * Send a bunch of metrics to StatsD server.
	 *
	 * @param metrics
	 *            the metrics to be sent
	 * @return a future that allows to hook on the operation completion
	 */
	CompletableFuture<Void> send(MetricValue... metrics);

	/**
	 * Creates a batch version of this client.
	 *
	 * The batch version will only send the recorded metrics when the
	 * {@link StatsDClient#close()} method is performed.
	 *
	 * Closing batch statsD client will NOT close the underlying client.
	 *
	 * @return the batched version
	 */
	StatsDClient batch();

	@Override
	public void close();

}
