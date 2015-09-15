package com.flozano.metrics.client.util;

import static java.util.Objects.requireNonNull;

import java.util.function.UnaryOperator;

import com.flozano.metrics.client.MetricValue;

public final class DuplicateWithPrefixPreprocessor implements UnaryOperator<MetricValue[]> {

	private final String prefix;

	public DuplicateWithPrefixPreprocessor(String prefix) {
		this.prefix = requireNonNull(prefix);
	}

	@Override
	public MetricValue[] apply(MetricValue[] value) {
		if (value == null) {
			throw new IllegalArgumentException();
		}
		if (value.length == 0) {
			return value;
		}
		MetricValue[] processed = new MetricValue[value.length * 2];
		for (int i = 0; i < value.length; i++) {
			processed[i] = value[i];
			processed[i + value.length] = process(value[i]);
		}

		return processed;
	}

	private MetricValue process(MetricValue metricValue) {
		return metricValue.withName(prefix + metricValue.getName());
	}

}
