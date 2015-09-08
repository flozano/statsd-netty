package com.flozano.metrics.client;

import static java.util.Objects.requireNonNull;

public abstract class MetricValue {
	private final long value;
	private final String name;
	private final Double sampleRate;
	private final String suffix;

	protected MetricValue(String name, long value, Double sampleRate, String suffix) {
		this.name = requireNonNull(name);
		this.value = value;
		this.sampleRate = validateSampleRate(sampleRate);
		this.suffix = suffix;
	}

	public boolean isSignRequiredInValue() {
		return false;
	}

	public static Double validateSampleRate(Double sampleRate) {
		if (sampleRate == null) {
			return null;
		}
		if (sampleRate < 0 || sampleRate > 1) {
			throw new IllegalArgumentException("sample rate must be between 0 and 1");
		}

		return sampleRate;
	}

	public String getName() {
		return name;
	}

	public Double getSampleRate() {
		return sampleRate;
	}

	public long getValue() {
		return value;
	}

	public String getSuffix() {
		return suffix;
	}


	public abstract MetricValue withRate(double rate);

	public abstract MetricValue withName(String string);
}
