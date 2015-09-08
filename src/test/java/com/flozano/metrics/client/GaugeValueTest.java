package com.flozano.metrics.client;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.flozano.metrics.client.GaugeValue;

public class GaugeValueTest {

	@Test
	public void discreteValue() {
		assertEquals("gauge:1234|g", new GaugeValue("gauge", 1234).toString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void discreteNegativeValue() {
		new GaugeValue("gauge", -1234);
	}

	@Test
	public void deltaValue() {
		assertEquals("gauge:+1234|g",
				new GaugeValue("gauge", 1234, true).toString());
	}

	@Test
	public void deltaNegativeValue() {
		assertEquals("gauge:-1234|g",
				new GaugeValue("gauge", -1234, true).toString());
	}
}
