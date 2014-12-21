package com.flozano.statsd.metrics;

public class Count extends Metric {

	public Count(String name, long value, Double sample) {
		super(name, value, sample);
	}

	public Count(String name, long value) {
		this(name, value, null);
	}

	@Override
	public String getSuffix() {
		return "c";
	}

}
