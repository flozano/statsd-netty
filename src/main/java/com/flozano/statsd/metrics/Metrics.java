package com.flozano.statsd.metrics;

import static java.util.Objects.requireNonNull;

import java.time.Clock;

import com.flozano.statsd.client.StatsDClient;

public class Metrics {

	private final StatsDClient client;
	private final Clock clock;

	public Metrics(StatsDClient client, Clock clock) {
		this.client = requireNonNull(client);
		this.clock = requireNonNull(clock);
	}

	public Timer timer(String name) {
		return new Timer(requireNonNull(name), client, clock);
	}

	public Counter counter(String name) {
		return new Counter(requireNonNull(name), client);
	}

	public Gauge gauge(String name) {
		return new Gauge(requireNonNull(name), client);
	}
}
