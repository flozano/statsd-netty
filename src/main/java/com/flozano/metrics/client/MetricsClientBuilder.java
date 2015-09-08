package com.flozano.metrics.client;

import java.util.function.UnaryOperator;

import com.flozano.metrics.client.statsd.StatsDMetricsClientBuilderImpl;

import io.netty.channel.EventLoopGroup;

/**
 * Builder for MetricsClient
 *
 * @author flozano
 *
 */
public interface MetricsClientBuilder {

	/**
	 * Creates a new StatsD client builder
	 *
	 */
	static MetricsClientBuilder statsd() {
		return new StatsDMetricsClientBuilderImpl();
	}

	/**
	 * Sets the sample rate of elements that will be actually sent.
	 *
	 * Eg: if rate is 0.75d, more or les 75% of items will be sent (and the
	 * \@0.75 suffix will be added to the metrics)
	 *
	 */
	MetricsClientBuilder withSampleRate(Double rate);

	/**
	 * Sets the rate at which the client will flush the pending metric values to
	 * the network.
	 *
	 * Eg: if a rate is 0.1d, 1 in 10 writes will flush the pending metrics.
	 *
	 */
	MetricsClientBuilder withFlushRate(double rate);

	/**
	 * The StatsD hostname that will receive the metric values
	 */
	MetricsClientBuilder withHost(String host);

	/**
	 * The UDP port where the server is listening to.
	 */
	MetricsClientBuilder withPort(int port);

	/**
	 * Allow to provide a pre-created EventLoopGroup.
	 *
	 * If provided, when {@link MetricsClient#close()} is invoked, the
	 * EventLoopGroup will NOT be shutdown.
	 *
	 */
	MetricsClientBuilder withEventLoopGroup(EventLoopGroup eventLoopGroup);

	/**
	 * Optional preprocessor to perform additional processing over the metrics
	 * before they're sent to the wire.
	 *
	 * @param preprocessor
	 * @return
	 */
	MetricsClientBuilder withPreprocessor(UnaryOperator<MetricValue[]> preprocessor);

	/**
	 * @return a newly configured StatsD client.
	 */
	MetricsClient build();

}