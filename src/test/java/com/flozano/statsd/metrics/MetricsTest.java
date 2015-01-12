package com.flozano.statsd.metrics;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Clock;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.flozano.statsd.client.StatsDClient;
import com.flozano.statsd.metrics.Timer.Ongoing;
import com.flozano.statsd.metrics.values.CountValue;
import com.flozano.statsd.metrics.values.GaugeValue;
import com.flozano.statsd.metrics.values.TimingValue;

public class MetricsTest {

	@Mock
	StatsDClient client;
	Clock clock = Clock.systemUTC();
	Metrics metrics;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		metrics = new Metrics(client, clock);
	}

	@Test
	public void gaugeValue() {
		ArgumentCaptor<GaugeValue> argument = ArgumentCaptor
				.forClass(GaugeValue.class);
		metrics.gauge("gauge").value(1234l);
		verify(client, times(1)).send(argument.capture());
		assertEquals("gauge", argument.getValue().getName());
		assertEquals(1234l, argument.getValue().getValue());
		assertFalse(argument.getValue().isDelta());
	}

	@Test
	public void gaugeDelta() {
		ArgumentCaptor<GaugeValue> argument = ArgumentCaptor
				.forClass(GaugeValue.class);
		metrics.gauge("gauge").delta(1234l);
		verify(client, times(1)).send(argument.capture());
		assertEquals("gauge", argument.getValue().getName());
		assertEquals(1234l, argument.getValue().getValue());
		assertTrue(argument.getValue().isDelta());
	}

	@Test
	public void counter() {
		ArgumentCaptor<CountValue> argument = ArgumentCaptor
				.forClass(CountValue.class);
		metrics.counter("counter").count(123l);
		verify(client, times(1)).send(argument.capture());
		assertEquals("counter", argument.getValue().getName());
		assertEquals(123l, argument.getValue().getValue());
	}

	@Test
	public void timer() throws InterruptedException {
		long waiting = 1000;
		long margin = 100;
		ArgumentCaptor<TimingValue> argument = ArgumentCaptor
				.forClass(TimingValue.class);
		try (Ongoing o = metrics.timer("timer").time()) {
			Thread.sleep(waiting);
		}
		verify(client, times(1)).send(argument.capture());
		assertEquals("timer", argument.getValue().getName());
		assertThat(
				argument.getValue().getValue(),
				allOf(greaterThanOrEqualTo(waiting - margin),
						lessThanOrEqualTo(waiting + margin)));

	}
}