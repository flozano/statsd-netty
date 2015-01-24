package com.flozano.statsd.metrics;

import static java.util.Objects.requireNonNull;

final class PrefixedMetrics implements Metrics {

	private final Metrics inner;

	private final String prefix;

	public PrefixedMetrics(Metrics inner, String prefix) {
		this.inner = requireNonNull(inner);
		this.prefix = requireNonNull(prefix);
	}

	private final String[] checkAndAppend(String[] partz) {
		if (partz == null) {
			throw new IllegalArgumentException();
		}
		if (partz.length == 0) {
			throw new IllegalArgumentException();
		}
		if (partz[0] == null) {
			throw new IllegalArgumentException();
		}
		String[] result = new String[partz.length + 1];
		result[0] = prefix;
		System.arraycopy(partz, 0, result, 1, partz.length);
		return result;
	}

	@Override
	public Timer timer(String... name) {
		return inner.timer(checkAndAppend(name));
	}

	@Override
	public Counter counter(String... name) {
		return inner.counter(checkAndAppend(name));
	}

	@Override
	public Gauge gauge(String... name) {
		return inner.gauge(checkAndAppend(name));
	}

	@Override
	public Metrics batch() {
		return new PrefixedMetrics(inner.batch(), prefix);
	}

	@Override
	public void close() {
		inner.close();
	}

}
