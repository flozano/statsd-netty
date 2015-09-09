package com.flozano.metrics;

public interface Counter extends Metric {

	/**
	 * Add the provided value to this counter
	 */
	void count(long value);

	/**
	 * Add 1 to this counter
	 */
	default void hit() {
		count(1);
	}

	/**
	 * Increment this counter by the provided value
	 *
	 * @param value
	 *            the value
	 */
	default void inc(long value) {
		count(Math.abs(value));
	}

	/**
	 * Decrement this counter by the provided value
	 *
	 * @param value
	 *            the value
	 */
	default void dec(long value) {
		count(-1 * Math.abs(value));
	}

}