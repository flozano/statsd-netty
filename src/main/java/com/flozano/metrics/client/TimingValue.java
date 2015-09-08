package com.flozano.metrics.client;

public final class TimingValue extends MetricValue {

	private static final String SUFFIX = "ms";

	public TimingValue(String name, long value, Double sample) {
		super(name, value, sample, SUFFIX);
	}

	public TimingValue(String name, long value) {
		this(name, value, null);
	}

	@Override
	public MetricValue withRate(double rate) {
		return new TimingValue(getName(), getValue(), rate);
	}

	@Override
	public MetricValue withName(String name) {
		return new TimingValue(name, getValue(), getSampleRate());
	}

}
