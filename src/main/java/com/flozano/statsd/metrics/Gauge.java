package com.flozano.statsd.metrics;

public class Gauge extends Metric {

	public Gauge(String name, long value, Double sample) {
		super(name, value, sample);
	}

	@Override
	public String getSuffix() {
		return "g";
	}

}
