package com.flozano.statsd.client.netty;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.flozano.statsd.metrics.values.MetricValue;

public class MetricNameTest {

	@Test
	public void simple() {
		assertEquals("testing", MetricValue.name("testing"));
		assertEquals("testing", MetricValue.name("testing", null));
		assertEquals("testing", MetricValue.name("testing", (String[]) null));
	}

	@Test(expected = NullPointerException.class)
	public void badNull() {
		MetricValue.name(null);
	}

	@Test
	public void composed() {
		assertEquals("testing.part1.part2",
				MetricValue.name("testing", "part1", "part2"));
		assertEquals("testing.part1.part2",
				MetricValue.name("testing", "part1", null, "part2"));
	}

}
