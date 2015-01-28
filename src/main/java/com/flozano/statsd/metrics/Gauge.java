package com.flozano.statsd.metrics;

import java.util.concurrent.TimeUnit;
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
	 * Configure this gauge to gather values periodically.
	 * 
	 * @param supplier
	 *            The supplier function that will provide values
	 * @param period
	 *            The period for the supplying of values
	 * @param unit
	 *            The unit of the period.
	 */
	void supply(Supplier<Long> supplier, long period, TimeUnit unit);
}