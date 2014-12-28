package com.flozano.statsd.netty;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.flozano.statsd.metrics.Metric;

public class MetricNameTest {

	@Test
	public void simple() {
		assertEquals("testing", Metric.name("testing"));
		assertEquals("testing", Metric.name("testing", null));
		assertEquals("testing", Metric.name("testing", (String[]) null));
	}

	@Test(expected = NullPointerException.class)
	public void badNull() {
		Metric.name(null);
	}

	@Test
	public void composed() {
		assertEquals("testing.part1.part2",
				Metric.name("testing", "part1", "part2"));
		assertEquals("testing.part1.part2",
				Metric.name("testing", "part1", null, "part2"));
	}

}
