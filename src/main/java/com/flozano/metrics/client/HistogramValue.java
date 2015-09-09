package com.flozano.metrics.client;

import com.flozano.metrics.Tags;

/**
 *
 * @author flozano
 *
 */
public class HistogramValue extends MetricValue {

	private static final String SUFFIX = "h";

	public HistogramValue(String name, long value, Double sampleRate, Tags tags) {
		super(name, value, sampleRate, SUFFIX, tags);
	}

	public HistogramValue(String name, long value, Tags tags) {
		this(name, value, null, tags);
	}

	@Override
	public MetricValue withRate(double rate) {
		return new HistogramValue(getName(), getValue(), rate, getTags());
	}

	@Override
	public MetricValue withName(String name) {
		return new HistogramValue(name, getValue(), getSampleRate(), getTags());
	}

}
