package com.flozano.metrics;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public interface Gauge extends Metric {

	/**
	 * Report a discrete value for this gauge
	 *
	 * @param value
	 *            The value
	 */
	void value(long value);

	/**
	 * Report an increment/decrement for this gauge
	 *
	 * @param value
	 *            The delta value
	 */
	void delta(long value);

	/**
	 * Configure this gauge to gather values periodically from a supplier.
	 *
	 * @param time
	 *            The period
	 * @param unit
	 *            The units of the period
	 * @param supplier
	 *            the value supplier
	 */
	void supply(long time, TimeUnit unit, Supplier<Long> supplier);

	/**
	 * Configure this gauge to gather values periodically from a supplier, with
	 * a pre-defined period.
	 *
	 * @param supplier
	 *            The value supplier
	 */
	default void supply(Supplier<Long> supplier) {
		supply(10, TimeUnit.SECONDS, supplier);
	}
}