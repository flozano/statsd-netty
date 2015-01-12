package com.flozano.statsd.metrics;

import com.flozano.statsd.client.StatsDClient;

public class Gauge {

	private final String name;
	private final StatsDClient client;

	Gauge(String name, StatsDClient client) {
		this.name = name;
		this.client = client;
	}

	public void value(long value) {
		client.send(new com.flozano.statsd.metrics.values.GaugeValue(name,
				value, false));
	}

	public void delta(long value) {
		client.send(new com.flozano.statsd.metrics.values.GaugeValue(name,
				value, true));
	}

}
