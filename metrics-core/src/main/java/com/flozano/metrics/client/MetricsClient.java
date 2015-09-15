package com.flozano.metrics.client;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Client for metrics
 *
 * @author flozano
 *
 */
public interface MetricsClient extends AutoCloseable {

	/**
	 * Send a bunch of metrics
	 *
	 * @param metrics
	 *            the metrics to be sent
	 * @return a future that allows to hook on the operation completion
	 */
	CompletableFuture<Void> send(MetricValue... metrics);

	/**
	 * Send a bunch of metrics
	 *
	 * @param metrics
	 *            the metrics to be sent
	 * @return a future that allows to hook on the operation completion
	 */
	default CompletableFuture<Void> send(Stream<MetricValue> metrics) {
		List<MetricValue> values = metrics.collect(Collectors.toList());
		return send(values.toArray(new MetricValue[values.size()]));
	}

	/**
	 * Creates a batch version of this client.
	 *
	 * The batch version will only send the recorded metrics when the
	 * {@link MetricsClient#close()} method is performed.
	 *
	 * Closing batch statsD client will NOT close the underlying client.
	 *
	 * @return the batched version
	 */
	MetricsClient batch();

	@Override
	public void close();

}
