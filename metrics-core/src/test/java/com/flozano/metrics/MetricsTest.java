package com.flozano.metrics;

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
import static org.mockito.Mockito.when;

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

import com.flozano.metrics.Timer.TimeKeeping;
import com.flozano.metrics.client.CountValue;
import com.flozano.metrics.client.GaugeValue;
import com.flozano.metrics.client.HistogramValue;
import com.flozano.metrics.client.MetricsClient;
import com.flozano.metrics.client.TimingValue;

public class MetricsTest {

	@Mock
	MetricsClient client;
	Clock clock = Clock.systemUTC();
	Metrics metrics;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		metrics = MetricsBuilder.create().withClient(client).withClock(null).withPrefix("pr3fix").build().tagged("xyz",
				"abcd");
		when(client.batch()).thenReturn(client);
	}

	@After
	public void tearDown() {
		metrics.close();
	}

	@Test
	public void measureValueAsTime() {
		ArgumentCaptor<TimingValue> argument = ArgumentCaptor.forClass(TimingValue.class);

		MetricsBuilder.create().withClient(client).withClock(null).withMeasureAsTime().build().tagged("xyz", "abcd")
				.measure("measure").value(54321);

		verify(client, times(1)).send(argument.capture());
		assertEquals("measure", argument.getValue().getName());
		assertEquals(54321, argument.getValue().getValue());
		assertTrue(argument.getValue().getTags().has("xyz"));
	}

	@Test
	public void measureValueAsHistogram() {
		ArgumentCaptor<HistogramValue> argument = ArgumentCaptor.forClass(HistogramValue.class);

		MetricsBuilder.create().withClient(client).withClock(null).withMeasureAsHistogram().build()
				.tagged("xyz", "abcd").measure("measure").value(54321);

		verify(client, times(1)).send(argument.capture());
		assertEquals("measure", argument.getValue().getName());
		assertEquals(54321, argument.getValue().getValue());
		assertTrue(argument.getValue().getTags().has("xyz"));

	}

	@Test
	public void gaugeValue() {
		ArgumentCaptor<GaugeValue> argument = ArgumentCaptor.forClass(GaugeValue.class);
		metrics.gauge("gauge").value(1234l);
		verify(client, times(1)).send(argument.capture());
		assertEquals("pr3fix.gauge", argument.getValue().getName());
		assertEquals(1234l, argument.getValue().getValue());
		assertFalse(argument.getValue().isDelta());
		assertTrue(argument.getValue().getTags().has("xyz"));

	}

	@Test
	public void gaugeValueBatch() {
		ArgumentCaptor<GaugeValue> argument = ArgumentCaptor.forClass(GaugeValue.class);
		try (Metrics batch = metrics.batch()) {
			batch.gauge("gauge").value(1234l);
		}
		verify(client, times(1)).send(argument.capture());
		assertEquals("pr3fix.gauge", argument.getValue().getName());
		assertEquals(1234l, argument.getValue().getValue());
		assertFalse(argument.getValue().isDelta());
		assertTrue(argument.getValue().getTags().has("xyz"));

	}

	@Test
	public void suppliedGaugeWithoutValues() throws InterruptedException {
		metrics.gauge("gauge").supply(100, TimeUnit.MILLISECONDS, () -> null);
		Thread.sleep(3100);
		verifyNoMoreInteractions(client);
	}

	@Test
	public void suppliedGauge() {
		ArgumentCaptor<GaugeValue> argument = ArgumentCaptor.forClass(GaugeValue.class);
		Queue<Long> values = new LinkedList<Long>(Arrays.asList(10l, 20l, 30l));
		metrics.gauge("gauge").supply(100, TimeUnit.MILLISECONDS, () -> values.poll());
		await().atMost(4, TimeUnit.SECONDS).until(() -> values.isEmpty());
		verify(client, times(3)).send(argument.capture());
		List<GaugeValue> captured = argument.getAllValues();
		for (GaugeValue v : captured) {
			assertEquals("pr3fix.gauge", v.getName());
			assertFalse(v.isDelta());
		}
		assertTrue(captured.get(0).getTags().has("xyz"));
		assertTrue(captured.get(1).getTags().has("xyz"));
		assertTrue(captured.get(2).getTags().has("xyz"));

		assertEquals(10l, captured.get(0).getValue());
		assertEquals(20l, captured.get(1).getValue());
		assertEquals(30l, captured.get(2).getValue());

	}

	@Test
	public void gaugeValueComplexName() {
		ArgumentCaptor<GaugeValue> argument = ArgumentCaptor.forClass(GaugeValue.class);
		metrics.gauge("gauge", "more", "complex").value(1234l);
		verify(client, times(1)).send(argument.capture());
		assertEquals("pr3fix.gauge.more.complex", argument.getValue().getName());
		assertEquals(1234l, argument.getValue().getValue());
		assertFalse(argument.getValue().isDelta());
	}

	@Test
	public void gaugeDelta() {
		ArgumentCaptor<GaugeValue> argument = ArgumentCaptor.forClass(GaugeValue.class);
		metrics.gauge("gauge").delta(1234l);
		verify(client, times(1)).send(argument.capture());
		assertEquals("pr3fix.gauge", argument.getValue().getName());
		assertEquals(1234l, argument.getValue().getValue());
		assertTrue(argument.getValue().isDelta());
	}

	@Test
	public void counter() {
		ArgumentCaptor<CountValue> argument = ArgumentCaptor.forClass(CountValue.class);
		metrics.counter("counter").count(123l);
		verify(client, times(1)).send(argument.capture());
		assertEquals("pr3fix.counter", argument.getValue().getName());
		assertEquals(123l, argument.getValue().getValue());
	}

	@Test
	public void timer() throws InterruptedException {
		long waiting = 1000;
		long margin = 100;
		ArgumentCaptor<TimingValue> argument = ArgumentCaptor.forClass(TimingValue.class);
		try (TimeKeeping o = metrics.timer("timer").time()) {
			Thread.sleep(waiting);
		}
		verify(client, times(1)).send(argument.capture());
		assertEquals("pr3fix.timer", argument.getValue().getName());
		assertThat(argument.getValue().getValue(),
				allOf(greaterThanOrEqualTo(waiting - margin), lessThanOrEqualTo(waiting + margin)));

	}

	@Test
	public void multipleTimer() throws InterruptedException {
		long waiting = 1000;
		long margin = 100;
		ArgumentCaptor<TimingValue> argument = ArgumentCaptor.forClass(TimingValue.class);
		try (TimeKeeping o = metrics.multi(metrics.timer("timer"), metrics.timer("timer2")).time()) {
			Thread.sleep(waiting);
		}
		verify(client, times(2)).send(argument.capture());
		assertEquals("pr3fix.timer", argument.getAllValues().get(0).getName());
		assertEquals("pr3fix.timer2", argument.getAllValues().get(1).getName());

		assertThat(argument.getAllValues().get(0).getValue(),
				allOf(greaterThanOrEqualTo(waiting - margin), lessThanOrEqualTo(waiting + margin)));
		assertThat(argument.getAllValues().get(1).getValue(),
				allOf(greaterThanOrEqualTo(waiting - margin), lessThanOrEqualTo(waiting + margin)));
		assertEquals(argument.getAllValues().get(0).getValue(), argument.getAllValues().get(1).getValue());

	}

	@Test
	public void timerValue() {
		ArgumentCaptor<TimingValue> argument = ArgumentCaptor.forClass(TimingValue.class);
		metrics.timer("timer").time(123l);
		verify(client, times(1)).send(argument.capture());
		assertEquals("pr3fix.timer", argument.getValue().getName());
		assertEquals(123l, argument.getValue().getValue());
	}
}
