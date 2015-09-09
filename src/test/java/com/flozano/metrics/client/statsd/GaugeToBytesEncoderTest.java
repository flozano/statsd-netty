package com.flozano.metrics.client.statsd;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.flozano.metrics.client.GaugeValue;

public class GaugeToBytesEncoderTest {

	@Test
	public void discreteValue() {
		assertEquals("gauge:1234|g", MetricToBytesEncoder.toString(new GaugeValue("gauge", 1234)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void discreteNegativeValue() {
		new GaugeValue("gauge", -1234);
	}

	@Test
	public void deltaValue() {
		assertEquals("gauge:+1234|g", MetricToBytesEncoder.toString(new GaugeValue("gauge", 1234, true)));
	}

	@Test
	public void deltaNegativeValue() {
		assertEquals("gauge:-1234|g", MetricToBytesEncoder.toString(new GaugeValue("gauge", -1234, true)));
	}
}
