package com.flozano.statsd.client;

import static com.flozano.statsd.test.PredicateMatcher.compliesWith;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.flozano.statsd.values.GaugeValue;
import com.flozano.statsd.values.MetricValue;
import com.google.common.math.DoubleMath;

@RunWith(Parameterized.class)
public class RatedClientTest {

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { 1000, 0.5d }, { 2000, 0.2d },
				{ 5000, 0.8d }, { 10000, 0.1d } });
	}

	StatsDClient inner;
	RatedStatsDClient ratedClient;

	int iterations;
	Double sampleRate;
	int expectedItems;

	public RatedClientTest(int iterations, Double sampleRate) {
		this.iterations = iterations;
		this.sampleRate = sampleRate;
		this.expectedItems = (int) (iterations * sampleRate);
	}

	@Before
	public void setUp() {
		inner = mock(StatsDClient.class);
		ratedClient = new RatedStatsDClient(inner, randomDoubleProvider(),
				sampleRate);
	}

	@Test
	public void testReceiveWithRate() {
		for (int i = 0; i < iterations; i++) {
			ratedClient.send(new GaugeValue("test", 1234));
		}

		verify(inner, times(expectedItems)).send(
				argThat(compliesWith(
						(MetricValue v) -> DoubleMath.fuzzyEquals(sampleRate,
								v.getSampleRate(), 0.001d),
						"Received sample rate is wrong")));
	}

	public Supplier<Double> randomDoubleProvider() {
		Random r = new Random();
		LinkedList<Double> values = new LinkedList<>();
		int lowerCount = (int) (iterations * sampleRate);
		int upperCount = iterations - lowerCount;
		for (int i = 0; i < lowerCount; i++) {
			values.add(r.nextDouble() * sampleRate);
		}
		for (int i = 0; i < upperCount; i++) {
			values.add(r.nextDouble() * sampleRate + sampleRate);
		}
		assert (values.size() == iterations);
		return () -> values.poll();
	}
}
