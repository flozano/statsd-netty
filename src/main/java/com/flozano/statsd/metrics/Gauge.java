package com.flozano.statsd.metrics;

import java.util.function.Supplier;

public interface Gauge extends Metric {

	/**
	 * Report a discrete value for this gauge
	 */
	void value(long value);

	/**
	 * Report an increment/decrement for this gauge
	 * 
	 * @param value
	 */
	void delta(long value);

	/**
	 * Configure this gauge to gather values periodically from a supplier.
	 *
	 */
	void supply(Supplier<Long> supplier);
}