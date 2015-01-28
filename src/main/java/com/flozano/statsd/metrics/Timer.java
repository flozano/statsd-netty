package com.flozano.statsd.metrics;

public interface Timer extends Metric {

	TimeKeeping time();
	
	void time(long value);

	public interface TimeKeeping extends AutoCloseable {

		@Override
		public void close();
	}

}