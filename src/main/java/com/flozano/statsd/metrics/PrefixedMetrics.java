package com.flozano.statsd.metrics;

import static java.util.Objects.requireNonNull;

final class PrefixedMetrics implements Metrics {

	private final Metrics inner;

	private final String prefix;

	public PrefixedMetrics(Metrics inner, String prefix) {
		this.inner = requireNonNull(inner);
		this.prefix = requireNonNull(prefix);
	}

	private final String checkAndAppend(CharSequence[] partz) {
		if (partz == null) {
			throw new IllegalArgumentException();
		}
		if (partz.length == 0) {
			throw new IllegalArgumentException();
		}
		if (partz[0] == null) {
			throw new IllegalArgumentException();
		}
		if (partz.length == 1) {
			return inner.metricName(prefix, partz[0]);
		}
		return inner.metricName(prefix, inner.metricName(partz));
	}

	@Override
	public Timer timer(CharSequence... name) {
		return inner.timer(checkAndAppend(name));
	}

	@Override
	public Counter counter(CharSequence... name) {
		return inner.counter(checkAndAppend(name));
	}

	@Override
	public Gauge gauge(CharSequence... name) {
		return inner.gauge(checkAndAppend(name));
	}

	@Override
	public Measure measure(CharSequence... name) {
		return inner.measure(checkAndAppend(name));
	}

	@Override
	public Metrics batch() {
		return new PrefixedMetrics(inner.batch(), prefix);
	}

	@Override
	public void close() {
		inner.close();
	}

	@Override
	public Timer multi(Timer... timers) {
		return inner.multi(timers);
	}

}
