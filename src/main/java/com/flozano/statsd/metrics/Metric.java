package com.flozano.statsd.metrics;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;

public abstract class Metric {
	private final long value;
	private final String name;
	private final Double sampleRate;

	public Metric(String name, long value, Double sampleRate) {
		this.name = requireNonNull(name);
		this.value = value;
		this.sampleRate = validateSampleRate(sampleRate);
	}

	private static Double validateSampleRate(Double sampleRate) {
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

	public abstract String getSuffix();

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		toStringParts((part) -> sb.append(part));
		return sb.toString();
	}

	public void toStringParts(Consumer<String> parts) {
		parts.accept(name);
		parts.accept(":");
		parts.accept(Long.toString(getValue()));
		parts.accept("|");
		parts.accept(getSuffix());
		if (sampleRate != null) {
			parts.accept("|@");
			parts.accept(String.format("%1.2f", sampleRate));
		}
	}

	public static String name(String name, String... moreNames) {
		final StringBuilder sb = new StringBuilder(requireNonNull(name));
		if (moreNames != null && moreNames.length > 0) {
			for (String additional : moreNames) {
				if (additional != null) {
					sb.append('.').append(additional);
				}
			}
		}
		return sb.toString();
	}
}
