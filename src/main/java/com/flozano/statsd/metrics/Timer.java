package com.flozano.statsd.metrics;

import static java.util.Objects.requireNonNull;

import java.time.Clock;

import com.flozano.statsd.client.StatsDClient;
import com.flozano.statsd.metrics.values.TimingValue;

public class Timer {

	private final StatsDClient client;
	private final String name;
	private final Clock clock;

	Timer(String name, StatsDClient client, Clock clock) {
		this.clock = requireNonNull(clock);
		this.client = requireNonNull(client);
		this.name = requireNonNull(name);
	}

	public Ongoing time() {
		return new Ongoing();
	}

	public class Ongoing implements AutoCloseable {

		private long startTime;

		public Ongoing() {
			startTime = clock.millis();
		}

		@Override
		public void close() throws Exception {
			long elapsed = clock.millis() - startTime;
			client.send(new TimingValue(name, elapsed));
		}

	}
}
