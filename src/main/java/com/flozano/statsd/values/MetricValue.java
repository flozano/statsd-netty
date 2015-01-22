package com.flozano.statsd.values;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;

public abstract class MetricValue {
	private final long value;
	private final String name;
	private final Double sampleRate;
	private final String suffix;

	public MetricValue(String name, long value, Double sampleRate, String suffix) {
		this.name = requireNonNull(name);
		this.value = value;
		this.sampleRate = validateSampleRate(sampleRate);
		this.suffix = suffix;
	}

	protected boolean isSignRequiredInValue() {
		return false;
	}

	public static Double validateSampleRate(Double sampleRate) {
		if (sampleRate == null) {
			return null;
		}
		if (sampleRate < 0 || sampleRate > 1) {
			throw new IllegalArgumentException(
					"sample rate must be between 0 and 1");
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

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		toStringParts((part) -> sb.append(part));
		return sb.toString();
	}

	public void toStringParts(Consumer<String> parts) {
		parts.accept(name);
		parts.accept(":");
		if (isSignRequiredInValue() && getValue() > 0) {
			parts.accept("+");
		}
		parts.accept(Long.toString(getValue()));
		parts.accept("|");
		parts.accept(getSuffix());
		if (sampleRate != null) {
			parts.accept("|@");
			parts.accept(String.format("%1.2f", sampleRate));
		}
	}

	public abstract MetricValue withRate(double rate);
}
