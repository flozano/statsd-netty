package com.flozano.statsd.metrics;

import java.util.function.Supplier;

interface BackgroundReporter extends AutoCloseable {

	void addGauge(Gauge gauge, Supplier<Long> producer);

	@Override
	void close();

}