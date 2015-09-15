package com.flozano.metrics;

import java.time.Clock;
import java.util.Optional;
import java.util.function.Function;

import com.flozano.metrics.client.MetricsClient;
import com.flozano.metrics.client.NoOpMetricsClient;

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
		MetricsClient statsdClient = client.orElse(new NoOpMetricsClient());
		Metrics m = new MetricsImpl(statsdClient, clock.orElse(DEFAULT_CLOCK), measureAsTime, Optional.empty());
		return prefix.map((Function<String, Metrics>) (prefix) -> new PrefixedMetrics(m, prefix)).orElse(m);
	}
}
