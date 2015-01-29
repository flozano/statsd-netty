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
			<version>0.1.4</version>
		</dependency>
```


Example code:

```java
		try (Metrics metrics = MetricsBuilder.create()
				.withClient((clientBuilder) -> //
						clientBuilder.withHost("127.0.0.1") //
								.withPort(8125) //
								.withSampleRate(0.5) // send 50% of metrics only
				).withClock(Clock.systemUTC()).build()) {

			// Send a counter metric immediately
			metrics.counter("visitors").hit();

			// Create a batch of metrics that will be sent at the end of the try
			// block.
			try (Metrics batch = metrics.batch()) {
				batch.gauge("activeDatabaseConnections").value(
						getConnectionsFromPool());
				batch.gauge("activeSessions").delta(-1);
			}

			// Schedule a couple of gauges to be reported every 60 seconds
			metrics.gauge("databaseConnectionPool", "activeConnections")
					.supply(60, TimeUnit.SECONDS,
							() -> getConnectionsFromPool());

			metrics.gauge("databaseConnectionPool", "waitingForConnection")
					.supply(1, TimeUnit.MINUTES,
							() -> getWaitingForConnection());

			// Measure the time spent inside the try block
			try (TimeKeeping o = metrics.timer("timeSpentSavingData").time()) {
				saveData();
			}
		}
```
(note that you should keep the "Metrics" instance open for the whole lifecycle of your application).


Copyright 2014 Francisco A. Lozano López


[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/flozano/statsd-netty/trend.png)](https://bitdeli.com/free "Bitdeli Badge")

