package com.flozano.metrics.client;

import com.flozano.metrics.Tags;

public final class GaugeValue extends MetricValue {

	private static final String SUFFIX = "g";

	private final boolean delta;

	public GaugeValue(String name, long value, Double sample, boolean delta, Tags tags) {
		super(name, value, sample, SUFFIX, tags);
		this.delta = delta;
		if (!delta && value < 0) {
			throw new IllegalArgumentException("A negative number is not a valid absolute gauge value");
		}
	}

	public GaugeValue(String name, long value, Double sample, boolean delta) {
		this(name, value, sample, delta, Tags.empty());
	}

	public GaugeValue(String name, long value, Double sample, Tags tags) {
		this(name, value, sample, false, tags);
	}

	public GaugeValue(String name, long value, Double sample) {
		this(name, value, sample, Tags.empty());
	}

	public GaugeValue(String name, long value, boolean delta, Tags tags) {
		this(name, value, null, delta, tags);
	}

	public GaugeValue(String name, long value, boolean delta) {
		this(name, value, delta, Tags.empty());
	}

	public GaugeValue(String name, long value, Tags tags) {
		this(name, value, false, tags);
	}

	public GaugeValue(String name, long value) {
		this(name, value, false, Tags.empty());
	}

	public boolean isDelta() {
		return delta;
	}

	@Override
	public boolean isSignRequiredInValue() {
		return isDelta();
	}

	@Override
	public MetricValue withRate(double rate) {
		return new GaugeValue(getName(), getValue(), rate, delta, getTags());
	}

	@Override
	public MetricValue withName(String name) {
		return new GaugeValue(name, getValue(), getSampleRate(), delta, getTags());
	}

}
