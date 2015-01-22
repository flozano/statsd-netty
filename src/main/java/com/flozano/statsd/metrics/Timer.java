package com.flozano.statsd.metrics;

public interface Timer extends Metric {

	TimeKeeping time();

	public interface TimeKeeping extends AutoCloseable {

		@Override
		public void close();
	}

}