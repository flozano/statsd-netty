package com.flozano.statsd.client;

import io.netty.channel.EventLoopGroup;

final class ClientBuilderImpl implements ClientBuilder {
	private Double sampleRate = null;
	private double flushRate = 0.5d;
	private String host = "127.0.0.1";
	private int port = 8125;
	private EventLoopGroup eventLoopGroup;

	@Override
	public ClientBuilder withSampleRate(Double sampleRate) {
		this.sampleRate = sampleRate;
		return this;
	}

	@Override
	public ClientBuilder withFlushRate(double flushRate) {
		this.flushRate = flushRate;
		return this;
	}

	@Override
	public ClientBuilder withHost(String host) {
		this.host = host;
		return this;
	}

	@Override
	public ClientBuilder withPort(int port) {
		this.port = port;
		return this;
	}

	@Override
	public ClientBuilder withEventLoopGroup(EventLoopGroup eventLoopGroup) {
		this.eventLoopGroup = eventLoopGroup;
		return this;
	}

	@Override
	public StatsDClient build() {
		NettyStatsDClientImpl impl = eventLoopGroup == null ? new NettyStatsDClientImpl(
				host, port, flushRate) : new NettyStatsDClientImpl(host, port,
				eventLoopGroup, flushRate);
		if (sampleRate != null) {
			return new RatedStatsDClient(impl, sampleRate);
		} else {
			return impl;
		}
	}
}
