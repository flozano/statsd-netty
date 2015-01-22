package com.flozano.statsd.metrics;

public interface Gauge extends Metric {

	void value(long value);

	void delta(long value);

}