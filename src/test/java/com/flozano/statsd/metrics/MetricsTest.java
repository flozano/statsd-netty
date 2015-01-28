package com.flozano.statsd.metrics;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.time.Clock;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.flozano.statsd.client.StatsDClient;
import com.flozano.statsd.metrics.Timer.TimeKeeping;
import com.flozano.statsd.values.CountValue;
import com.flozano.statsd.values.GaugeValue;
import com.flozano.statsd.values.HistogramValue;
import com.flozano.statsd.values.TimingValue;

public class MetricsTest {

	@Mock
	StatsDClient client;
	Clock clock = Clock.systemUTC();
	Metrics metrics;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		metrics = MetricsBuilder.create().withClient(client).withClock(null)
				.withPrefix("pr3fix").build();
	}

	@After
	public void tearDown() {
		metrics.close();
	}

	@Test
	public void measureValueAsTime() {
		ArgumentCaptor<TimingValue> argument = ArgumentCaptor
				.forClass(TimingValue.class);

		MetricsBuilder.create().withClient(client).withClock(null)
				.withMeasureAsTime().build().measure("measure").value(54321);

		verify(client, times(1)).send(argument.capture());
		assertEquals("measure", argument.getValue().getName());
		assertEquals(54321, argument.getValue().getValue());
	}

	@Test
	public void measureValueAsHistogram() {
		ArgumentCaptor<HistogramValue> argument = ArgumentCaptor
				.forClass(HistogramValue.class);

		MetricsBuilder.create().withClient(client).withClock(null)
				.withMeasureAsHistogram().build().measure("measure")
				.value(54321);

		verify(client, times(1)).send(argument.capture());
		assertEquals("measure", argument.getValue().getName());
		assertEquals(54321, argument.getValue().getValue());
	}

	@Test
	public void gaugeValue() {
		ArgumentCaptor<GaugeValue> argument = ArgumentCaptor
				.forClass(GaugeValue.class);
		metrics.gauge("gauge").value(1234l);
		verify(client, times(1)).send(argument.capture());
		assertEquals("pr3fix.gauge", argument.getValue().getName());
		assertEquals(1234l, argument.getValue().getValue());
		assertFalse(argument.getValue().isDelta());
	}

	@Test
	public void suppliedGaugeWithoutValues() throws InterruptedException {
		metrics.gauge("gauge").supply(() -> null, 50, TimeUnit.MILLISECONDS);
		Thread.sleep(1000);
		verifyNoMoreInteractions(client);
	}

	@Test
	public void suppliedGauge() {
		ArgumentCaptor<GaugeValue> argument = ArgumentCaptor
				.forClass(GaugeValue.class);
		Queue<Long> values = new LinkedList<Long>(Arrays.asList(10l, 20l, 30l));
		metrics.gauge("gauge").supply(() -> values.poll(), 100,
				TimeUnit.MILLISECONDS);
		await().atMost(500, TimeUnit.MILLISECONDS)
				.until(() -> values.isEmpty());
		verify(client, times(3)).send(argument.capture());
		List<GaugeValue> captured = argument.getAllValues();
		for (GaugeValue v : captured) {
			assertEquals("pr3fix.gauge", v.getName());
			assertFalse(v.isDelta());
		}

		assertEquals(10l, captured.get(0).getValue());
		assertEquals(20l, captured.get(1).getValue());
		assertEquals(30l, captured.get(2).getValue());
	}

	@Test
	public void gaugeValueComplexName() {
		ArgumentCaptor<GaugeValue> argument = ArgumentCaptor
				.forClass(GaugeValue.class);
		metrics.gauge("gauge", "more", "complex").value(1234l);
		verify(client, times(1)).send(argument.capture());
		assertEquals("pr3fix.gauge.more.complex", argument.getValue().getName());
		assertEquals(1234l, argument.getValue().getValue());
		assertFalse(argument.getValue().isDelta());
	}

	@Test
	public void gaugeDelta() {
		ArgumentCaptor<GaugeValue> argument = ArgumentCaptor
				.forClass(GaugeValue.class);
		metrics.gauge("gauge").delta(1234l);
		verify(client, times(1)).send(argument.capture());
		assertEquals("pr3fix.gauge", argument.getValue().getName());
		assertEquals(1234l, argument.getValue().getValue());
		assertTrue(argument.getValue().isDelta());
	}

	@Test
	public void counter() {
		ArgumentCaptor<CountValue> argument = ArgumentCaptor
				.forClass(CountValue.class);
		metrics.counter("counter").count(123l);
		verify(client, times(1)).send(argument.capture());
		assertEquals("pr3fix.counter", argument.getValue().getName());
		assertEquals(123l, argument.getValue().getValue());
	}

	@Test
	public void timer() throws InterruptedException {
		long waiting = 1000;
		long margin = 100;
		ArgumentCaptor<TimingValue> argument = ArgumentCaptor
				.forClass(TimingValue.class);
		try (TimeKeeping o = metrics.timer("timer").time()) {
			Thread.sleep(waiting);
		}
		verify(client, times(1)).send(argument.capture());
		assertEquals("pr3fix.timer", argument.getValue().getName());
		assertThat(
				argument.getValue().getValue(),
				allOf(greaterThanOrEqualTo(waiting - margin),
						lessThanOrEqualTo(waiting + margin)));

	}

	@Test
	public void timerValue() {
		ArgumentCaptor<TimingValue> argument = ArgumentCaptor
				.forClass(TimingValue.class);
		metrics.timer("timer").time(123l);
		verify(client, times(1)).send(argument.capture());
		assertEquals("pr3fix.timer", argument.getValue().getName());
		assertEquals(123l, argument.getValue().getValue());
	}
}
