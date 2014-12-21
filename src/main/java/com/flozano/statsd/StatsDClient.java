package com.flozano.statsd;

import com.flozano.statsd.metrics.Metric;

public interface StatsDClient {

	void send(Metric... metrics);

}
