package com.flozano.statsd.metrics;

import com.flozano.statsd.util.NameComposer;

/**
 * Façade for managing metrics
 *
 * @author flozano
 *
 */
public interface Metrics extends AutoCloseable {

	/**
	 * @return a named timer
	 */
	Timer timer(CharSequence... name);

	/**
	 * @return a named counter
	 */
	Counter counter(CharSequence... name);

	/**
	 * @return a named gauge
	 */
	Gauge gauge(CharSequence... name);

	/**
	 * 
	 * @return a named measure.
	 * 
	 */
	Measure measure(CharSequence... name);

	/**
	 * @return a composed metric names
	 */
	default String metricName(CharSequence... names) {
		return NameComposer.composeName(names);
	}

	/**
	 * Create. a batched version of this Metrics.
	 *
	 * The batched version will not send the metrics until the
	 * {@link Metrics#close()} method is invoked.
	 *
	 * Closing the returned Metrics will NOT close this one.
	 */
	Metrics batch();

	@Override
	public void close();

	Timer multi(com.flozano.statsd.metrics.Timer... timers);

}