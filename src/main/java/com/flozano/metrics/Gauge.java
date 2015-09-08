package com.flozano.metrics;

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
	 * Configure this gauge to gather values periodically from a supplier.
	 *
	 */
	void supply(long time, TimeUnit unit, Supplier<Long> supplier);

	default void supply(Supplier<Long> supplier) {
		supply(10, TimeUnit.SECONDS, supplier);
	}
}