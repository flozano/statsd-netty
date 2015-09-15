package com.flozano.metrics;

import java.util.concurrent.TimeUnit;

public interface Timer extends Metric {

	/**
	 * Return an active time-keeping instance.
	 *
	 * To be used in a try() {} block.
	 *
	 * @return the time-keeping object
	 */
	TimeKeeping time();

	/**
	 * Report a discrete time value in milliseconds.
	 *
	 * @param value
	 *            the value
	 */
	void time(long value);

	/**
	 * Report a discrete time value in specified units
	 *
	 * @param value
	 *            the value to report
	 * @param unit
	 *            the unit of the value
	 */
	default void time(long value, TimeUnit unit) {
		time(unit.toMillis(value));
	}

	public interface TimeKeeping extends AutoCloseable {

		@Override
		public void close();
	}

}