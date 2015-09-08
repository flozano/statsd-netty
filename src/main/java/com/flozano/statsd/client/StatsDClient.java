package com.flozano.statsd.client;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	 * Send a bunch of metrics to StatsD server
	 *
	 * @param map
	 * @return
	 */
	default CompletableFuture<Void> send(Stream<MetricValue> metrics) {
		List<MetricValue> values = metrics.collect(Collectors.toList());
		return send(values.toArray(new MetricValue[values.size()]));
	}

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
