package com.flozano.statsd.client;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flozano.statsd.values.MetricValue;

final class RatedStatsDClient implements StatsDClient {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(StatsDClient.class);

	private final StatsDClient inner;
	private final double rate;
	private final Supplier<Double> randomDoubleSupplier;

	public RatedStatsDClient(StatsDClient inner, double rate) {
		this(inner, rate, ThreadLocalRandom::current);
	}

	public RatedStatsDClient(StatsDClient inner, double rate,
			Supplier<Random> randomSupplier) {
		this(inner, () -> randomSupplier.get().nextDouble(), rate);
	}

	public RatedStatsDClient(StatsDClient inner,
			Supplier<Double> randomDoubleSupplier, double rate) {
		this.inner = requireNonNull(inner);
		this.randomDoubleSupplier = requireNonNull(randomDoubleSupplier);
		this.rate = MetricValue.validateSampleRate(rate);
	}

	@Override
	public CompletableFuture<Void> send(MetricValue... metrics) {
		if (metrics == null || metrics.length == 0) {
			return CompletableFuture.completedFuture(null);
		}
		MetricValue[] transformed = transformed(metrics);
		if (transformed.length == 0) {
			return CompletableFuture.completedFuture(null);
		}
		return inner.send(transformed);
	}

	private MetricValue[] transformed(MetricValue[] metrics) {
		if (rate == 1.0) {
			return metrics;
		}
		ArrayList<MetricValue> output = new ArrayList<MetricValue>();
		for (MetricValue metric : metrics) {
			double randomValue = randomDoubleSupplier.get();
			if (randomValue <= rate) {
				output.add(metric.withRate(rate));
			} else {
				LOGGER.trace(
						"Metric discarded: random value is greater than rate (randomValue={}, rate={}, metric={}",
						randomValue, rate, metric);
			}
		}
		return output.toArray(new MetricValue[output.size()]);
	}

	@Override
	public void close() {
		inner.close();
	}

	@Override
	public StatsDClient batch() {
		return new RatedStatsDClient(new BatchStatsDClient(inner), rate);
	}
}
