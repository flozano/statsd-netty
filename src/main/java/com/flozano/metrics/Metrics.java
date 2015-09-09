package com.flozano.metrics;

import com.flozano.metrics.client.util.NameComposer;

/**
 * Façade for managing metrics
 *
 * @author flozano
 *
 */
public interface Metrics extends AutoCloseable {

	/**
	 * @return a named timer
	 * @param name
	 *            the name
	 */
	Timer timer(CharSequence... name);

	/**
	 * @return a named counter
	 * @param name
	 *            the mame
	 *
	 */
	Counter counter(CharSequence... name);

	/**
	 * @return a named gauge
	 * @param name
	 *            the name
	 */
	Gauge gauge(CharSequence... name);

	/**
	 *
	 * @return a named measure.
	 * @param name
	 *            the name
	 *
	 */
	Measure measure(CharSequence... name);

	/**
	 * @return a composed metric names
	 * @param names
	 *            the names
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
	 *
	 * @return the batched metrics
	 */
	Metrics batch();

	@Override
	public void close();

	/**
	 * Multiple timers tied onto one
	 *
	 * @param timers
	 *            the timers to tie
	 * @return the tied timer
	 */
	Timer multi(com.flozano.metrics.Timer... timers);

	/**
	 * Returns a 'tagged' version of the metrics object, that will attach this
	 * tag to the outgoing metrics
	 *
	 * @param name
	 *            the tag name
	 * @param value
	 *            the tag value
	 * @return the tagged version of the metrics object
	 */
	Metrics tagged(CharSequence name, CharSequence value);

}