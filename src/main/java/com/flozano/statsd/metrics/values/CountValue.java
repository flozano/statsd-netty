package com.flozano.statsd.metrics.values;

public class CountValue extends MetricValue {

	private static final String SUFFIX = "c";

	public CountValue(String name, long value, Double sample) {
		super(name, value, sample, SUFFIX);
	}

	public CountValue(String name, long value) {
		this(name, value, null);
	}

	@Override
	public MetricValue withRate(double rate) {
		return new CountValue(getName(), getValue(), rate);
	}

}
