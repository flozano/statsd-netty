package com.flozano.metrics.client.log;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.flozano.metrics.client.GaugeValue;
import com.flozano.metrics.client.MetricsClient;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class NonRepeatingGaugeLogMetricsClientTest {

	@Mock
	Logger logger;

	MetricsClient client;

	@Before
	public void setUp() {

		MockitoAnnotations.initMocks(this);
		Cache<String, String> c = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).maximumSize(1000)
				.build();
		client = new NonRepeatingGaugeLogMetricsClient(logger, c.asMap()::put);
	}

	@Test
	public void testTwoInvocationsOfSameValue() {
		client.send(new GaugeValue("x", 1l), new GaugeValue("x", 1l));
		verify(logger, times(1)).info(anyString());
		verifyNoMoreInteractions(logger);
	}

	@Test
	public void testTwoInvocationsOfDifferentValue() {
		client.send(new GaugeValue("x", 1l), new GaugeValue("x", 2l));
		verify(logger, times(2)).info(anyString());
		verifyNoMoreInteractions(logger);
	}

	@Test
	public void testThreeInvocationsOfSameValueAndDifferentValue() {
		client.send(new GaugeValue("x", 1l), new GaugeValue("x", 2l), new GaugeValue("x", 1l));
		verify(logger, times(3)).info(anyString());
		verifyNoMoreInteractions(logger);
	}
}
