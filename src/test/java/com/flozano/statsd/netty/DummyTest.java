package com.flozano.statsd.netty;

import java.io.IOException;

import org.junit.Test;

import com.flozano.statsd.metrics.Count;

public class DummyTest {
	@Test
	public void test() throws IOException, InterruptedException {
		NettyStatsDClientImpl c = new NettyStatsDClientImpl("127.0.0.1", 8125);
		for (int i = 0; i < 100; i++) {
			c.send(new Count("example", 10));
		}
		Thread.sleep(1000);
		c.close();
	}
}
