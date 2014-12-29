package com.flozano.statsd.metrics;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class MetricTest {

	@Parameters(name = "{index}: type={0}, name={1}, value={2}, sampleRate={3}")
	public static Collection<Object[]> params() {
		List<Object[]> params = new LinkedList<>();
		for (Class<? extends Metric> c : Arrays.asList(Gauge.class,
				Count.class, Timing.class)) {
			for (String name : Arrays.asList("some", "thing")) {
				for (long value : Arrays.asList(100, 100000, 20000)) {
					for (Double sampleRate : Arrays.<Double> asList(0.1, 0.5,
							1d, null)) {
						params.add(new Object[] { c, name, value, sampleRate });
					}
				}
			}
		}

		return params;
	}

	Class<? extends Metric> type;

	String name = "something";
	long value;
	Double sampleRate;

	public MetricTest(Class<? extends Metric> type, String name, long value,
			Double sampleRate) {
		this.type = type;
		this.name = name;
		this.value = value;
		this.sampleRate = sampleRate;
	}

	@Test
	public void toStringTest() {
		Metric metric = newMetric();
		assertThat(metric.toString(),
				is(equalTo(expectedValue(metric.getSuffix()))));
	}

	@Test
	public void getNameTest() {
		assertThat(newMetric().getName(), is(equalTo(name)));
	}

	@Test
	public void getValueTest() {
		assertThat(newMetric().getValue(), is(equalTo(value)));
	}

	@Test
	public void getSampleRateTest() {
		assertThat(newMetric().getSampleRate(), is(equalTo(sampleRate)));
	}

	String expectedValue(String suffix) {
		if (sampleRate == null) {
			return String.format("%s:%d|%s", name, value, suffix);
		}
		return String.format("%s:%d|%s|@%.2f", name, value, suffix, sampleRate);
	}

	Metric newMetric() {
		try {
			return type.getConstructor(String.class, long.class, Double.class)
					.newInstance(name, value, sampleRate);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
