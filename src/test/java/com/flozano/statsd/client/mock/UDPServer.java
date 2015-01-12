package com.flozano.statsd.client.mock;

import java.util.List;

public interface UDPServer extends AutoCloseable {

	List<String> getItemsSnapshot();

	void clear();

	boolean waitForAllItemsReceived();

}