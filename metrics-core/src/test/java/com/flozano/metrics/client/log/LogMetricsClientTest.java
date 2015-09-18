package com.flozano.metrics.client.log;

import static org.mockito.AdditionalMatchers.find;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Before;
import org.junit.Test;
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
		verify(logger, times(1)).info(find("^time:(.*)\tm:x\tmytag:myvalue\tother:abc\tc:1234"));
		verifyNoMoreInteractions(logger);
	}

	@Test
	public void testSimple() {
		client.send(new CountValue("x", 1234));

		client.send(new CountValue("y", 1234), new CountValue("z", 1234));
		// verify(logger, times(3)).info(eq(LogMetricsClient.FORMAT), (Object[])
		// anyVararg());
		verify(logger).info(find("^time:(.*)\tm:x\tc:1234"));
		verify(logger).info(find("^time:(.*)\tm:y\tc:1234"));
		verify(logger).info(find("^time:(.*)\tm:z\tc:1234"));

		verifyNoMoreInteractions(logger);
	}

	@Test
	public void testBatch() {
		try (MetricsClient c = client.batch()) {
			client.send(new CountValue("x", 1234));
			client.send(new CountValue("y", 1234), new CountValue("z", 1234));
		}
		verify(logger).info(find("^time:(.*)\tm:x\tc:1234"));
		verify(logger).info(find("^time:(.*)\tm:y\tc:1234"));
		verify(logger).info(find("^time:(.*)\tm:z\tc:1234"));
		verifyNoMoreInteractions(logger);
	}
}
