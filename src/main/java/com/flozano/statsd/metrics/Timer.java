package com.flozano.statsd.metrics;

import java.util.concurrent.TimeUnit;

public interface Timer extends Metric {

	/**
	 * Return an active time-keeping instance.
	 * 
	 * To be used in a try() {} block.
	 */
	TimeKeeping time();

	/**
	 * Report a discrete time value in milliseconds.
	 */
	void time(long value);

	/**
	 * Report a discrete time value in specified units
	 */
	default void time(long value, TimeUnit unit) {
		time(unit.toMillis(value));
	}

	public interface TimeKeeping extends AutoCloseable {

		@Override
		public void close();
	}

}