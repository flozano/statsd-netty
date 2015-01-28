package com.flozano.statsd.metrics;

public interface Measure extends Metric {
	void value(long value);
}
