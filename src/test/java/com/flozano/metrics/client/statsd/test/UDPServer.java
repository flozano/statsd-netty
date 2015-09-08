package com.flozano.metrics.client.statsd.test;

import java.util.List;

public interface UDPServer extends AutoCloseable {

	List<String> getItemsSnapshot();

	void clear();

	boolean waitForAllItemsReceived();

}