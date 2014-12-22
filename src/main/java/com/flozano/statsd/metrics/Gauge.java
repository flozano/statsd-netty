package com.flozano.statsd.metrics;

public class Gauge extends Metric {

	public Gauge(String name, long value, Double sample) {
		super(name, value, sample);
	}
	
	public Gauge(String name, long value) {
		this(name, value, null);
	}

	@Override
	public String getSuffix() {
		return "g";
	}

}
