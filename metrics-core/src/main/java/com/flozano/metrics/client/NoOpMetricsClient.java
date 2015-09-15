package com.flozano.metrics.client;

import java.util.concurrent.CompletableFuture;

public final class NoOpMetricsClient implements MetricsClient {
	@Override
	public CompletableFuture<Void> send(MetricValue... metrics) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public MetricsClient batch() {
		return this;
	}

	@Override
	public void close() {

	}
}
