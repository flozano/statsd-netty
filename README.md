statsd-netty
============
[![Build Status](https://secure.travis-ci.org/flozano/statsd-netty.svg?branch=master)](https://travis-ci.org/flozano/statsd-netty)

A Netty-based statsd client with Apache 2.0 License.

It requires Java 8, netty and SLF4J.

How to use:
----------
Maven dependency:
```xml
		<dependency>
			<groupId>com.flozano.statsd-netty</groupId>
			<artifactId>statsd-netty</artifactId>
			<version>0.0.4</version>
		</dependency>
```


Example code:

```java
		// Indicates how likely the writes will flush to the statsd server
		int rateOfFlush = 80;

		// Metrics class allows auto-closing of resources
		try (Metrics metrics = new Metrics(new NettyStatsDClientImpl(
				"127.0.0.1", 8125, rateOfFlush))) {
			metrics.counter("visitors").hit();
			metrics.counter("soldItems").count(25);
			metrics.gauge("activeDatabaseConnections").value(
					getConnectionsFromPool());
			metrics.gauge("activeSessions").delta(-1);
			try (Ongoing o = metrics.timer("timeSpentSavingData").time()) {
				saveData();
			}
		}
```



Copyright 2014 Francisco A. Lozano López


[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/flozano/statsd-netty/trend.png)](https://bitdeli.com/free "Bitdeli Badge")

