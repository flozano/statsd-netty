package com.flozano.metrics.client.log;

import static java.util.Objects.requireNonNull;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.slf4j.Logger;

import com.flozano.metrics.Tags.Tag;
import com.flozano.metrics.client.MetricValue;
import com.flozano.metrics.client.MetricsClient;

public class LogMetricsClient implements MetricsClient {
	private final Logger logger;

	public LogMetricsClient(Logger logger) {
		this.logger = requireNonNull(logger);
	}

	@Override
	public CompletableFuture<Void> send(MetricValue... metrics) {
		String t = time();
		for (MetricValue m : metrics) {
			metric(t, m);
		}
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Void> send(Stream<MetricValue> metrics) {
		String t = time();
		metrics.forEach(m -> metric(t, m));
		return CompletableFuture.completedFuture(null);
	}

	private void metric(String t, MetricValue m) {
		String key = formatKey(m);
		String value = formatValue(m);
		StringBuilder sb = new StringBuilder("time:").append(t).append(key).append(value);
		apply(m, key, sb.toString());
	}

	protected void apply(MetricValue m, String key, String value) {
		logger.info(value);
	}

	private String formatValue(MetricValue m) {
		StringBuilder sb = new StringBuilder("\t").append(m.getCode()).append(':').append(m.getValue());
		if (m.getSampleRate() != null && m.getSampleRate() < 1) {
			sb.append("\tr:").append(m.getSampleRate());
		}
		return sb.toString();
	}

	private String formatKey(MetricValue m) {
		StringBuilder sb = new StringBuilder("\tm:").append(m.getName());
		m.getTags().stream().map(this::formatTag).forEach(sb::append);
		return sb.toString();
	}

	private CharSequence formatTag(Tag tag) {
		return new StringBuilder("\t").append(tag.name).append(':').append(tag.value);
	}

	@Override
	public MetricsClient batch() {
		return new Batch();
	}

	@Override
	public void close() {

	}

	private static String time() {
		return Instant.now().toString();
	}

	private class Batch implements MetricsClient {

		List<MetricValue> pending = new LinkedList<>();
		String t = Instant.now().toString();

		@Override
		public CompletableFuture<Void> send(MetricValue... metrics) {
			for (MetricValue m : metrics) {
				pending.add(m);
			}
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public MetricsClient batch() {
			return this;
		}

		@Override
		public void close() {
			for (MetricValue m : pending) {
				metric(t, m);
			}
		}

	}
}
