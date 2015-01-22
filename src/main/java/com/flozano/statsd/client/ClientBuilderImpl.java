package com.flozano.statsd.client;

final class ClientBuilderImpl implements ClientBuilder {
	private Double sampleRate = null;
	private double flushRate = 0.5d;
	private String host = "127.0.0.1";
	private int port = 8125;

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
	public StatsDClient build() {
		NettyStatsDClientImpl impl = new NettyStatsDClientImpl(host, port,
				flushRate);
		if (sampleRate != null) {
			return new RatedStatsDClient(impl, sampleRate);
		} else {
			return impl;
		}
	}
}
