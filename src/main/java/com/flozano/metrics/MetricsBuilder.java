package com.flozano.metrics;

import java.time.Clock;
import java.util.function.UnaryOperator;

import com.flozano.metrics.client.MetricsClient;
import com.flozano.metrics.client.statsd.StatsDMetricsClientBuilder;

/**
 * Builder for Metrics
 *
 * @author flozano
 *
 */
public interface MetricsBuilder {

	/**
	 * Creates a new StatsD client builder
	 *
	 * @return a new builder
	 */
	static MetricsBuilder create() {
		return new MetricsBuilderImpl();
	}

	/**
	 * Set the metrics to use a specific client
	 *
	 * @return a new builder
	 */
	MetricsBuilder withClient(MetricsClient client);

	/**
	 * Set the metrics to use a newly configured client
	 *
	 * @return a new builder
	 *
	 * @param clientBuilderConfigurer
	 *            The configurator for the newly configured client
	 */
	MetricsBuilder withClient(UnaryOperator<StatsDMetricsClientBuilder> clientBuilderConfigurer);

	/**
	 * Set the metrics to use a specific clock instead of the system UTC-based
	 * one.
	 *
	 * @return a new builder
	 */
	MetricsBuilder withClock(Clock clock);

	/**
	 * Enforce the metrics to be prefixed by the specified String.
	 *
	 * @return a new builder
	 */
	MetricsBuilder withPrefix(String prefix);

	/**
	 * Send measure values as time values. Default behavior and most compatible.
	 *
	 * @return a new builder
	 */
	MetricsBuilder withMeasureAsTime();

	/**
	 * Send measure values as histogram values. Compatible with datadog.
	 *
	 * @return a new builder
	 */
	MetricsBuilder withMeasureAsHistogram();

	/**
	 * @return a newly configured Metrics instance
	 */
	Metrics build();

}