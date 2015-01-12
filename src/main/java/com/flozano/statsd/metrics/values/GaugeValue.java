package com.flozano.statsd.metrics.values;

public class GaugeValue extends MetricValue {

	private static final String SUFFIX = "g";

	private final boolean delta;

	public GaugeValue(String name, long value, Double sample, boolean delta) {
		super(name, value, sample, SUFFIX);
		this.delta = delta;
		if (!delta && value < 0) {
			throw new IllegalArgumentException(
					"A negative number is not a valid absolute gauge value");
		}
	}

	public GaugeValue(String name, long value, Double sample) {
		this(name, value, sample, false);
	}

	public GaugeValue(String name, long value, boolean delta) {
		this(name, value, null, delta);
	}

	public GaugeValue(String name, long value) {
		this(name, value, false);
	}

	public boolean isDelta() {
		return delta;
	}

	@Override
	protected boolean isSignRequiredInValue() {
		return isDelta();
	}

}
