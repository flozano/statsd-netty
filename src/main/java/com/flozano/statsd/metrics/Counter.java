package com.flozano.statsd.metrics;

import com.flozano.statsd.client.StatsDClient;
import com.flozano.statsd.metrics.values.CountValue;

public class Counter {
	private final StatsDClient client;
	private final String name;

	Counter(String name, StatsDClient client) {
		this.name = name;
		this.client = client;
	}

	public void count(long value) {
		client.send(new CountValue(name, value));
	}

	public void hit() {
		count(1l);
	}
}
