package com.flozano.statsd.client;

import com.flozano.statsd.client.netty.NettyStatsDClientImpl;

public final class ClientBuilder {
	private Double rate = null;
	private int flushProbability = 50;
	private String host = "127.0.0.1";
	private int port = 8125;

	public ClientBuilder withRate(double rate) {
		this.rate = rate;
		return this;
	}

	public ClientBuilder withFlushProbability(int flushProbability) {
		this.flushProbability = flushProbability;
		return this;
	}

	public ClientBuilder withHost(String host) {
		this.host = host;
		return this;
	}

	public ClientBuilder withPort(int port) {
		this.port = port;
		return this;
	}

	public StatsDClient buildClient() {
		NettyStatsDClientImpl impl = new NettyStatsDClientImpl(host, port,
				flushProbability);
		if (rate != null) {
			return new RatedStatsDClient(impl, rate);
		} else {
			return impl;
		}
	}
}
