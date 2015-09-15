package com.flozano.metrics.client.statsd;

import java.util.function.UnaryOperator;

import com.flozano.metrics.client.MetricValue;
import com.flozano.metrics.client.MetricsClient;

import io.netty.channel.EventLoopGroup;

final class StatsDMetricsClientBuilderImpl implements StatsDMetricsClientBuilder {
	private Double sampleRate = null;
	private double flushRate = 0.5d;
	private String host = "127.0.0.1";
	private int port = 8125;
	private EventLoopGroup eventLoopGroup;
	private UnaryOperator<MetricValue[]> processor = UnaryOperator.identity();

	@Override
	public StatsDMetricsClientBuilder withSampleRate(Double sampleRate) {
		this.sampleRate = sampleRate;
		return this;
	}

	@Override
	public StatsDMetricsClientBuilder withFlushRate(double flushRate) {
		this.flushRate = flushRate;
		return this;
	}

	@Override
	public StatsDMetricsClientBuilder withHost(String host) {
		this.host = host;
		return this;
	}

	@Override
	public StatsDMetricsClientBuilder withPort(int port) {
		this.port = port;
		return this;
	}

	@Override
	public StatsDMetricsClientBuilder withEventLoopGroup(EventLoopGroup eventLoopGroup) {
		this.eventLoopGroup = eventLoopGroup;
		return this;
	}

	@Override
	public MetricsClient build() {
		NettyStatsDClientImpl impl = eventLoopGroup == null
				? new NettyStatsDClientImpl(processor, host, port, flushRate)
				: new NettyStatsDClientImpl(processor, host, port, eventLoopGroup, flushRate);
		if (sampleRate != null) {
			return new RatedStatsDClient(impl, sampleRate);
		} else {
			return impl;
		}
	}

	@Override
	public StatsDMetricsClientBuilder withPreprocessor(UnaryOperator<MetricValue[]> processor) {
		this.processor = processor == null ? UnaryOperator.identity() : processor;
		return this;
	}
}
