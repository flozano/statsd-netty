package com.flozano.statsd.metrics;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.util.Optional;
import java.util.function.UnaryOperator;

import com.flozano.statsd.client.ClientBuilder;
import com.flozano.statsd.client.StatsDClient;

final class MetricsBuilderImpl implements MetricsBuilder {

	private static final Clock DEFAULT_CLOCK = Clock.systemUTC();

	private Optional<Clock> clock;

	private StatsDClient client;

	@Override
	public MetricsBuilder withClient(
			UnaryOperator<ClientBuilder> clientBuilderConfigurer) {
		this.client = requireNonNull(clientBuilderConfigurer).apply(
				ClientBuilder.create()).build();
		return this;
	}

	@Override
	public MetricsBuilder withClient(StatsDClient client) {
		this.client = requireNonNull(client);
		return this;
	}

	@Override
	public MetricsBuilder withClock(Clock clock) {
		this.clock = Optional.ofNullable(clock);
		return this;
	}

	@Override
	public Metrics build() {
		return new MetricsImpl(requireNonNull(client),
				clock.orElse(DEFAULT_CLOCK));
	}

}
