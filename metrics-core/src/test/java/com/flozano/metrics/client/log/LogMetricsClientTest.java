package com.flozano.metrics.client.log;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.flozano.metrics.Tags;
import com.flozano.metrics.client.CountValue;
import com.flozano.metrics.client.MetricsClient;

public class LogMetricsClientTest {

	@Mock
	Logger logger;

	LogMetricsClient client;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		client = new LogMetricsClient(logger);
	}

	@Test
	public void testRightParameters() {
		client.send(new CountValue("x", 1234, Tags.empty().with("mytag", "myvalue").with("other", "abc")));
		ArgumentCaptor<Object> varargsCaptor = ArgumentCaptor.forClass(Object.class);

		verify(logger, times(1)).info(eq(LogMetricsClient.FORMAT), (Object[]) varargsCaptor.capture());
		List<Object> values = varargsCaptor.getAllValues();
		assertTrue(Duration.between(Instant.parse((CharSequence) values.get(0)), Instant.now()).toMillis() < 100);
		assertEquals("x", values.get(1));
		assertEquals("c", values.get(2));
		assertEquals(1234l, values.get(3));
		assertEquals("\tmytag:myvalue\tother:abc", values.get(4));
	}

	@Test
	public void testSimple() {
		client.send(new CountValue("x", 1234));

		client.send(new CountValue("y", 1234), new CountValue("z", 1234));
		verify(logger, times(3)).info(eq(LogMetricsClient.FORMAT), (Object[]) anyVararg());
		verifyNoMoreInteractions(logger);
	}

	@Test
	public void testBatch() {
		try (MetricsClient c = client.batch()) {
			client.send(new CountValue("x", 1234));
			client.send(new CountValue("y", 1234), new CountValue("z", 1234));
		}

		verify(logger, times(3)).info(eq(LogMetricsClient.FORMAT), (Object[]) anyVararg());
		verifyNoMoreInteractions(logger);
	}
}
