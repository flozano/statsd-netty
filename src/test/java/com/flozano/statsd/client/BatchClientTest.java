package com.flozano.statsd.client;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import com.flozano.statsd.values.GaugeValue;
import com.flozano.statsd.values.MetricValue;
import com.flozano.statsd.values.TimingValue;

public class BatchClientTest {

	StatsDClient inner;

	@Before
	public void setUp() {
		inner = mock(StatsDClient.class);
		when(inner.send(metricValueVarArg())).thenReturn(CompletableFuture.completedFuture(null));
	}

	@Test(expected = IllegalStateException.class)
	public void testDoubleBatch() {
		try (BatchStatsDClient batchClient = new BatchStatsDClient(inner)) {
			try (StatsDClient c = batchClient.batch()) {
			}
		}
	}

	@Test
	public void testBatch() {
		List<MetricValue> metrics = Arrays.asList(new GaugeValue("test", 4321), new TimingValue("test2", 321));
		List<CompletableFuture<Void>> cfs = new ArrayList<>();

		CompletableFuture<Void> cf;
		try (BatchStatsDClient batchClient = new BatchStatsDClient(inner)) {
			for (MetricValue m : metrics) {
				cfs.add(batchClient.send(m));
			}
			cf = CompletableFuture.allOf(cfs.toArray(new CompletableFuture[cfs.size()]));
			assertFalse(cf.isDone());
			verifyNoMoreInteractions(inner);
		}
		await().atMost(5, TimeUnit.SECONDS).until(() -> cf.isDone());
		assertFalse(cf.isCancelled());
		assertFalse(cf.isCompletedExceptionally());
		verify(inner, times(1)).send(metricValueVarArg());
	}

	static MetricValue[] metricValueVarArg() {
		return anyVararg();
	}
}
