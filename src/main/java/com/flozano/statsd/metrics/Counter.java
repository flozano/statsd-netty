package com.flozano.statsd.metrics;

public interface Counter extends Metric {

	void count(long value);

	void hit();

}