package com.flozano.statsd.metrics;

public class Timing extends Metric {

	public Timing(String name, long value, Double sample) {
		super(name, value, sample);
	}
	
	public Timing(String name, long value) {
		this(name, value, null);
	}

	@Override
	public String getSuffix() {
		return "ms";
	}
}
