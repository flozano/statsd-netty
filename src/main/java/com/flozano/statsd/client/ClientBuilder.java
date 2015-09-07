package com.flozano.statsd.client;

import io.netty.channel.EventLoopGroup;

import java.util.function.UnaryOperator;

import com.flozano.statsd.values.MetricValue;

/**
 * Builder for StatsDClient
 *
 * @author flozano
 *
 */
public interface ClientBuilder {

	/**
	 * Creates a new StatsD client builder
	 *
	 */
	static ClientBuilder create() {
		return new ClientBuilderImpl();
	}

	/**
	 * Sets the sample rate of elements that will be actually sent.
	 *
	 * Eg: if rate is 0.75d, more or les 75% of items will be sent (and the
	 * \@0.75 suffix will be added to the metrics)
	 *
	 */
	ClientBuilder withSampleRate(Double rate);

	/**
	 * Sets the rate at which the client will flush the pending metric values to
	 * the network.
	 *
	 * Eg: if a rate is 0.1d, 1 in 10 writes will flush the pending metrics.
	 *
	 */
	ClientBuilder withFlushRate(double rate);

	/**
	 * The StatsD hostname that will receive the metric values
	 */
	ClientBuilder withHost(String host);

	/**
	 * The UDP port where the server is listening to.
	 */
	ClientBuilder withPort(int port);

	/**
	 * Allow to provide a pre-created EventLoopGroup.
	 *
	 * If provided, when {@link StatsDClient#close()} is invoked, the
	 * EventLoopGroup will NOT be shutdown.
	 *
	 */
	ClientBuilder withEventLoopGroup(EventLoopGroup eventLoopGroup);

	/**
	 * Optional preprocessor to perform additional processing over the metrics
	 * before they're sent to the wire.
	 * 
	 * @param preprocessor
	 * @return
	 */
	ClientBuilder withPreprocessor(UnaryOperator<MetricValue[]> preprocessor);

	/**
	 * @return a newly configured StatsD client.
	 */
	StatsDClient build();

}