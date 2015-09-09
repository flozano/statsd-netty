package com.flozano.metrics.client;

import com.flozano.metrics.Tags;

public final class TimingValue extends MetricValue {

	private static final String SUFFIX = "ms";

	public TimingValue(String name, long value, Double sample, Tags tags) {
		super(name, value, sample, SUFFIX, tags);
	}

	public TimingValue(String name, long value, Double sample) {
		super(name, value, sample, SUFFIX, Tags.empty());
	}

	public TimingValue(String name, long value, Tags tags) {
		this(name, value, null, tags);
	}

	public TimingValue(String name, long value) {
		this(name, value, Tags.empty());
	}

	@Override
	public MetricValue withRate(double rate) {
		return new TimingValue(getName(), getValue(), rate, getTags());
	}

	@Override
	public MetricValue withName(String name) {
		return new TimingValue(name, getValue(), getSampleRate(), getTags());
	}

}
