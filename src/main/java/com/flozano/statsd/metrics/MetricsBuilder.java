package com.flozano.statsd.metrics;

import java.time.Clock;
import java.util.function.UnaryOperator;

import com.flozano.statsd.client.ClientBuilder;
import com.flozano.statsd.client.StatsDClient;

public final class MetricsBuilder {

	private Clock clock;

	private UnaryOperator<ClientBuilder> clientBuilderConfigurer;

	public MetricsBuilder withClient(
			UnaryOperator<ClientBuilder> clientBuilderConfigurer) {
		this.clientBuilderConfigurer = clientBuilderConfigurer;
		return this;
	}

	public MetricsBuilder withClock(Clock clock) {
		this.clock = clock;
		return this;
	}

	private StatsDClient buildClient() {
		return clientBuilderConfigurer.apply(new ClientBuilder()).buildClient();
	}

	public Metrics buildMetrics() {
		return new Metrics(buildClient(), clock == null ? Clock.systemUTC()
				: clock);
	}

}
