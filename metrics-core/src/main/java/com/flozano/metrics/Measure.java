package com.flozano.metrics;

public interface Measure extends Metric {

	/**
	 * Record a new value for this measure.
	 *
	 * @param value
	 *            The value
	 */
	void value(long value);

}
