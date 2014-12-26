package com.flozano.statsd;

import java.util.concurrent.CompletableFuture;

import com.flozano.statsd.metrics.Metric;

public interface StatsDClient {

	CompletableFuture<Void> send(Metric... metrics);

}
