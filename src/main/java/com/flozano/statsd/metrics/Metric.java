package com.flozano.statsd.metrics;

import java.nio.charset.StandardCharsets;

public abstract class Metric {
	private final long value;
	private final String name;
	private final Double sampleRate;

	public Metric(String name, long value, Double sample) {
		this.name = name;
		this.value = value;
		this.sampleRate = sample;
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
