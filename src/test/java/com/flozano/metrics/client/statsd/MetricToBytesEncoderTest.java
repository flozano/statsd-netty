package com.flozano.metrics.client.statsd;

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

import com.flozano.metrics.client.CountValue;
import com.flozano.metrics.client.GaugeValue;
import com.flozano.metrics.client.MetricValue;
import com.flozano.metrics.client.TimingValue;

@RunWith(Parameterized.class)
public class MetricToBytesEncoderTest {
	@Parameters(name = "{index}: type={0}, name={1}, value={2}, sampleRate={3}")
	public static Collection<Object[]> params() {
		List<Object[]> params = new LinkedList<>();
		for (Class<? extends MetricValue> c : Arrays.asList(GaugeValue.class, CountValue.class, TimingValue.class)) {
			for (String name : Arrays.asList("some", "thing")) {
				for (long value : Arrays.asList(100, 100000, 20000)) {
					for (Double sampleRate : Arrays.<Double> asList(0.1, 0.5, 1d, null)) {
						params.add(new Object[] { c, name, value, sampleRate });
					}
				}
			}
		}

		return params;
	}

	Class<? extends MetricValue> type;

	String name = "something";
	long value;
	Double sampleRate;

	public MetricToBytesEncoderTest(Class<? extends MetricValue> type, String name, long value, Double sampleRate) {
		this.type = type;
		this.name = name;
		this.value = value;
		this.sampleRate = sampleRate;
	}

	@Test
	public void toStringPartsTest() {
		MetricValue metricValue = newMetric();
		StringBuilder sb = new StringBuilder();
		MetricToBytesEncoder.toStringParts(metricValue, sb::append);
		assertThat(sb.toString(), is(equalTo(expectedValue(metricValue.getSuffix()))));
	}

	String expectedValue(String suffix) {
		if (sampleRate == null) {
			return String.format("%s:%d|%s", name, value, suffix);
		}
		return String.format("%s:%d|%s|@%.2f", name, value, suffix, sampleRate);
	}

	MetricValue newMetric() {
		try {
			return type.getConstructor(String.class, long.class, Double.class).newInstance(name, value, sampleRate);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
