package com.flozano.statsd.metrics;

import java.time.Clock;
import java.util.function.UnaryOperator;

import com.flozano.statsd.client.ClientBuilder;
import com.flozano.statsd.client.StatsDClient;

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
	 */
	static MetricsBuilder create() {
		return new MetricsBuilderImpl();
	}

	/**
	 * Set the metrics to use a specific client
	 */
	MetricsBuilder withClient(StatsDClient client);

	/**
	 * Set the metrics to use a newly configured client
	 *
	 * @param clientBuilderConfigurer
	 *            The configurator for the newly configured client
	 */
	MetricsBuilder withClient(
			UnaryOperator<ClientBuilder> clientBuilderConfigurer);

	/**
	 * Set the metrics to use a specific clock instead of the system UTC-based
	 * one.
	 *
	 */
	MetricsBuilder withClock(Clock clock);

	/**
	 * Enforce the metrics to be prefixed by the specified String.
	 *
	 */
	MetricsBuilder withPrefix(String prefix);

	/**
	 * Send measure values as time values. Default behavior and most compatible.
	 * 
	 */
	MetricsBuilder withMeasureAsTime();

	/**
	 * Send measure values as histogram values. Compatible with datadog.
	 * 
	 */
	MetricsBuilder withMeasureAsHistogram();

	/**
	 * @return a newly configured Metrics instance
	 */
	Metrics build();

}