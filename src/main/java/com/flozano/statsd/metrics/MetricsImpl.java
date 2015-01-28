package com.flozano.statsd.metrics;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.flozano.statsd.client.StatsDClient;
import com.flozano.statsd.values.CountValue;
import com.flozano.statsd.values.GaugeValue;
import com.flozano.statsd.values.HistogramValue;
import com.flozano.statsd.values.TimingValue;

final class MetricsImpl implements AutoCloseable, Metrics {

	private final StatsDClient client;
	private final Clock clock;
	private final boolean measureAsTime;
	private final BackgroundReporter reporter;

	MetricsImpl(StatsDClient client, Clock clock, boolean measureAsTime) {
		this.client = requireNonNull(client);
		this.clock = requireNonNull(clock);
		this.measureAsTime = measureAsTime;
		this.reporter = new SimpleGaugeReporter();
	}

	@Override
	public TimerImpl timer(CharSequence... name) {
		return new TimerImpl(metricName(name));
	}

	@Override
	public Measure measure(CharSequence... name) {
		if (measureAsTime) {
			return new TimeMeasureImpl(metricName(name));
		} else {
			return new HistogramMeasureImpl(metricName(name));
		}
	}

	@Override
	public CounterImpl counter(CharSequence... name) {
		return new CounterImpl(metricName(name));
	}

	@Override
	public GaugeImpl gauge(CharSequence... name) {
		return new GaugeImpl(metricName(name));
	}

	@Override
	public void close() {
		client.close();
	}

	private class CounterImpl implements Counter {
		private final String name;

		private CounterImpl(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void count(long value) {
			client.send(new CountValue(name, value));
		}
	}

	private class GaugeImpl implements Gauge {

		private final String name;

		private GaugeImpl(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void value(long value) {
			client.send(new GaugeValue(name, value, false));
		}

		@Override
		public void delta(long value) {
			client.send(new GaugeValue(name, value, true));
		}

		@Override
		public void supply(Supplier<Long> supplier, long period, TimeUnit unit) {
			reporter.addGauge(this, supplier, period, unit);
		}

	}

	private class TimerImpl implements Timer {

		private final String name;

		private TimerImpl(String name) {
			this.name = requireNonNull(name);
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public TimeKeepingImpl time() {
			return new TimeKeepingImpl();
		}

		private class TimeKeepingImpl implements TimeKeeping {

			private long startTime;

			private TimeKeepingImpl() {
				startTime = clock.millis();
			}

			@Override
			public void close() {
				long elapsed = clock.millis() - startTime;
				TimerImpl.this.time(elapsed);
			}
		}

		@Override
		public void time(long value) {
			client.send(new TimingValue(name, value));
		}
	}

	private class TimeMeasureImpl implements Measure {

		private final String name;

		public TimeMeasureImpl(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void value(long value) {
			client.send(new TimingValue(name, value));
		}

	}

	private class HistogramMeasureImpl implements Measure {

		private final String name;

		public HistogramMeasureImpl(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void value(long value) {
			client.send(new HistogramValue(name, value));
		}

	}

	private class SimpleGaugeReporter implements BackgroundReporter {

		private final ScheduledExecutorService executor;

		SimpleGaugeReporter() {
			this.executor = Executors.newSingleThreadScheduledExecutor();
		}

		@Override
		public void addGauge(Gauge gauge, Supplier<Long> producer, long period,
				TimeUnit unit) {
			executor.scheduleAtFixedRate(() -> {
				Long value = producer.get();
				if (value != null) {
					gauge.value(value);
				}
			}, 0, period, unit);
		}

		@Override
		public void close() {
			executor.shutdownNow();
		}

	}

	@Override
	public Metrics batch() {
		return new MetricsImpl(client.batch(), clock, measureAsTime);
	}
}
