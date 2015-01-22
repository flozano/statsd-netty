package com.flozano.statsd.test.mockserver;

import java.util.List;

public interface UDPServer extends AutoCloseable {

	List<String> getItemsSnapshot();

	void clear();

	boolean waitForAllItemsReceived();

}