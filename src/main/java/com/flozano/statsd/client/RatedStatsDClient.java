package com.flozano.statsd.client;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import com.flozano.statsd.metrics.values.MetricValue;

public final class RatedStatsDClient implements StatsDClient {

	private final StatsDClient inner;
	private final double rate;
	private final Supplier<Random> randomSupplier;

	public RatedStatsDClient(StatsDClient inner, double rate) {
		this(inner, rate, ThreadLocalRandom::current);
	}

	public RatedStatsDClient(StatsDClient inner, double rate,
			Supplier<Random> randomSupplier) {
		this.inner = requireNonNull(inner);
		this.rate = MetricValue.validateSampleRate(rate);
		this.randomSupplier = randomSupplier;
	}

	@Override
	public CompletableFuture<Void> send(MetricValue... metrics) {
		if (metrics == null || metrics.length == 0) {
			return CompletableFuture.completedFuture(null);
		}
		return inner.send(transformed(metrics));
	}

	private MetricValue[] transformed(MetricValue[] metrics) {
		Random random = randomSupplier.get();
		ArrayList<MetricValue> output = new ArrayList<MetricValue>();
		for (MetricValue metric : metrics) {
			if (random.nextDouble() <= rate) {
				output.add(metric.withRate(rate));
			}
		}
		return output.toArray(new MetricValue[output.size()]);
	}

	@Override
	public void close() throws IOException {
		inner.close();
	}
}
