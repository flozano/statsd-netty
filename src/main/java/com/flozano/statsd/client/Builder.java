package com.flozano.statsd.client;

import com.flozano.statsd.client.netty.NettyStatsDClientImpl;

public final class Builder {
	private Double rate = null;
	private int flushProbability = 50;
	private String host = "127.0.0.1";
	private int port = 8125;

	public Builder withRate(double rate) {
		this.rate = rate;
		return this;
	}

	public Builder withFlushProbability(int flushProbability) {
		this.flushProbability = flushProbability;
		return this;
	}

	public Builder withHost(String host) {
		this.host = host;
		return this;
	}

	public Builder withPort(int port) {
		this.port = port;
		return this;
	}

	public StatsDClient build() {
		NettyStatsDClientImpl impl = new NettyStatsDClientImpl(host, port,
				flushProbability);
		if (rate != null) {
			return new RatedStatsDClient(impl, rate);
		} else {
			return impl;
		}
	}
}
