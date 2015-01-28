package com.flozano.statsd.metrics;

public interface Timer extends Metric {

	/**
	 * Return an active time-keeping instance.
	 * 
	 * To be used in a try() {} block.
	 */
	TimeKeeping time();

	/**
	 * Report a discrete time value.
	 */
	void time(long value);

	public interface TimeKeeping extends AutoCloseable {

		@Override
		public void close();
	}

}