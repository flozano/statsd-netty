package com.flozano.statsd.values;

/**
 * Only supported by datadog statsd it seems...
 * 
 * @author flozano
 *
 */
public class HistogramValue extends MetricValue {

	private static final String SUFFIX = "h";

	public HistogramValue(String name, long value, Double sampleRate) {
		super(name, value, sampleRate, SUFFIX);
	}

	public HistogramValue(String name, long value) {
		this(name, value, null);
	}

	@Override
	public MetricValue withRate(double rate) {
		return new HistogramValue(getName(), getValue(), rate);
	}

}
