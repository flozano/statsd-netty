package com.flozano.metrics;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.flozano.metrics.client.MetricsClientBuilder;
import com.flozano.metrics.client.MetricsClient;

final class MetricsBuilderImpl implements MetricsBuilder {

	private static final Clock DEFAULT_CLOCK = Clock.systemUTC();

	private Optional<Clock> clock = Optional.of(DEFAULT_CLOCK);

	private Optional<String> prefix = Optional.empty();

	private Optional<MetricsClient> client = Optional.empty();

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
	public MetricsBuilder withClient(UnaryOperator<MetricsClientBuilder> clientBuilderConfigurer) {
		this.client = Optional.of(requireNonNull(clientBuilderConfigurer).apply(MetricsClientBuilder.statsd()).build());
		return this;
	}

	@Override
	public MetricsBuilder withClient(MetricsClient client) {
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
		MetricsClient statsdClient = client.orElseGet(() -> MetricsClientBuilder.statsd().build());
		Metrics m = new MetricsImpl(statsdClient, clock.orElse(DEFAULT_CLOCK), measureAsTime, Optional.empty());
		return prefix.map((Function<String, Metrics>) (prefix) -> new PrefixedMetrics(m, prefix)).orElse(m);
	}
}
