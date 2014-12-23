package com.flozano.statsd.metrics;

import static java.util.Objects.requireNonNull;

import java.nio.charset.StandardCharsets;

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
		StringBuilder sb = new StringBuilder(name).append(':')
				.append(getValue()).append('|').append(getSuffix());
		if (sampleRate != null) {
			sb = sb.append("|@").append(sampleRate);
		}

		return sb.toString();
	}

	public byte[] getBytes() {
		return toString().getBytes(StandardCharsets.UTF_8);
	}
}
