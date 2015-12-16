package com.flozano.metrics;

import java.time.Clock;

import com.flozano.metrics.client.MetricsClient;

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
	 * @param client
	 *            the client
	 * @return a new builder
	 */
	MetricsBuilder withClient(MetricsClient client);

	/**
	 * Set the metrics to use a specific clock instead of the system UTC-based
	 * one.
	 *
	 * @param clock
	 *            the clock
	 * @return a new builder
	 */
	MetricsBuilder withClock(Clock clock);

	/**
	 * Enforce the metrics to be prefixed by the specified String.
	 *
	 * @param prefix
	 *            the prefix
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
	 * Send gauge values only if they have changed from previous value.
	 *
	 * @return
	 */
	MetricsBuilder withSmartGauges(boolean smartGauges);

	/**
	 * @return a newly configured Metrics instance
	 */
	Metrics build();

}