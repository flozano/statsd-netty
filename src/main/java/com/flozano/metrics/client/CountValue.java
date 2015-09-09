package com.flozano.metrics.client;

import com.flozano.metrics.Tags;

public final class CountValue extends MetricValue {

	private static final String SUFFIX = "c";

	public CountValue(String name, long value, Double sample, Tags tags) {
		super(name, value, sample, SUFFIX, tags);
	}

	public CountValue(String name, long value, Double sample) {
		this(name, value, sample, Tags.empty());
	}

	public CountValue(String name, long value, Tags tags) {
		this(name, value, null, tags);
	}

	public CountValue(String name, long value) {
		this(name, value, null, Tags.empty());
	}

	@Override
	public CountValue withRate(double rate) {
		return new CountValue(getName(), getValue(), rate, getTags());
	}

	@Override
	public CountValue withName(String name) {
		return new CountValue(name, getValue(), getSampleRate(), getTags());
	}

}
