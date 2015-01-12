package com.flozano.statsd.metrics;

import java.time.Clock;

import com.flozano.statsd.client.ClientBuilder;

public class MetricsBuilder extends ClientBuilder {

	private Clock clock = null;

	public Metrics buildMetrics() {
		return new Metrics(buildClient(), clock == null ? Clock.systemUTC()
				: clock);
	}

}
