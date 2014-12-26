package com.flozano.statsd.mock;

import java.util.List;

public interface UDPServer extends AutoCloseable {

	List<String> getItemsSnapshot();

	void clear();

	void waitForAllItemsReceived() throws InterruptedException;

}