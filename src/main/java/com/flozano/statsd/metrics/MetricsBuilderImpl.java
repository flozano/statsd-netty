package com.flozano.statsd.metrics;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.flozano.statsd.client.ClientBuilder;
import com.flozano.statsd.client.StatsDClient;

final class MetricsBuilderImpl implements MetricsBuilder {

	private static final Clock DEFAULT_CLOCK = Clock.systemUTC();

	private Optional<Clock> clock = Optional.of(DEFAULT_CLOCK);

	private Optional<String> prefix = Optional.empty();

	private Optional<StatsDClient> client = Optional.empty();

	private boolean measureAsTime = true;

	@Override
	public MetricsBuilder withMeasureAsTime() {
		this.measureAsTime = true;
		return this;
	}

	@Override
	public MetricsBuilder withMeasureAsHistogram() {
		this.measureAsTime = false;
		return this;
	}

	@Override
	public MetricsBuilder withClient(
			UnaryOperator<ClientBuilder> clientBuilderConfigurer) {
		this.client = Optional.of(requireNonNull(clientBuilderConfigurer)
				.apply(ClientBuilder.create()).build());
		return this;
	}

	@Override
	public MetricsBuilder withClient(StatsDClient client) {
		this.client = Optional.of(client);
		return this;
	}

	@Override
	public MetricsBuilder withPrefix(String prefix) {
		this.prefix = Optional.ofNullable(prefix);
		return this;
	}

	@Override
	public MetricsBuilder withClock(Clock clock) {
		this.clock = Optional.ofNullable(clock);
		return this;
	}

	@Override
	public Metrics build() {
		StatsDClient statsdClient = client.orElseGet(() -> ClientBuilder
				.create().build());
		Metrics m = new MetricsImpl(statsdClient, clock.orElse(DEFAULT_CLOCK),
				measureAsTime);
		return prefix.map(
				(Function<String, Metrics>) (prefix) -> new PrefixedMetrics(m,
						prefix)).orElse(m);
	}
}
