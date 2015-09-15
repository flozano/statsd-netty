package com.flozano.metrics.client.statsd;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flozano.metrics.client.MetricValue;
import com.flozano.metrics.client.MetricsClient;

final class BatchStatsDClient implements MetricsClient {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(MetricsClient.class);

	private final List<MetricValue> pending = new LinkedList<>();

	private final MetricsClient inner;

	private final CompletableFuture<Void> result = new CompletableFuture<>();

	private boolean sent = false;

	public BatchStatsDClient(MetricsClient inner) {
		this.inner = requireNonNull(inner);
	}

	@Override
	public void close() {
		if (sent) {
			throw new IllegalStateException("Batch already sent");
		}
		LOGGER.trace("Closing batch");
		if (pending.size() > 0) {
			inner.send(pending.toArray(new MetricValue[pending.size()]))
					.handle((r, e) -> {
						if (e != null) {
							result.completeExceptionally(e);
						} else {
							result.complete(r);
						}
						return null;
					});
		}
		sent = true;
	}

	@Override
	public CompletableFuture<Void> send(MetricValue... metrics) {
		if (metrics.length > 0) {
			pending.addAll(Arrays.asList(metrics));
		}
		return result;
	}

	@Override
	public MetricsClient batch() {
		throw new IllegalStateException("Already a batch");
	}

}
