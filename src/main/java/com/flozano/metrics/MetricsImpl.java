package com.flozano.metrics;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.util.Optional;
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

	private Tags tags;

	MetricsImpl(StatsDClient client, Clock clock, boolean measureAsTime, Optional<Tags> tags) {
		this.client = requireNonNull(client);
		this.clock = requireNonNull(clock);
		this.measureAsTime = measureAsTime;
		this.reporter = new SimpleGaugeReporter();
		this.tags = tags.orElseGet(() -> new Tags());
	}

	@Override
	public TimerImpl timer(CharSequence... name) {
		return new TimerImpl(metricName(name), tags);
	}

	@Override
	public Timer multi(Timer... timers) {
		return new MultipleTimerImpl(timers);
	}

	@Override
	public Measure measure(CharSequence... name) {
		if (measureAsTime) {
			return new TimeMeasureImpl(metricName(name), tags);
		} else {
			return new HistogramMeasureImpl(metricName(name), tags);
		}
	}

	@Override
	public CounterImpl counter(CharSequence... name) {
		return new CounterImpl(metricName(name), tags);
	}

	@Override
	public GaugeImpl gauge(CharSequence... name) {
		return new GaugeImpl(metricName(name), tags);
	}

	@Override
	public void close() {
		try {
			reporter.close();
		} finally {
			client.close();
		}
	}

	private class CounterImpl implements Counter {
		private final String name;

		private final Tags tags;

		private CounterImpl(String name, Tags tags) {
			this.name = name;
			this.tags = tags;
		}

		@Override
		public Tags getTags() {
			return tags;
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
		private final Tags tags;

		private GaugeImpl(String name, Tags tags) {
			this.name = name;
			this.tags = tags;
		}

		@Override
		public Tags getTags() {
			return tags;
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
		public void supply(long time, TimeUnit unit, Supplier<Long> supplier) {
			reporter.addGauge(this, supplier, time, unit);
		}

	}

	private class MultipleTimerImpl implements Timer {
		private final Timer[] timers;
		private String name = null;

		private MultipleTimerImpl(Timer[] timers) {
			this.timers = requireNonNull(timers);
			if (timers.length < 1) {
				throw new IllegalArgumentException();
			}
			if (timers[0] == null) {
				throw new IllegalArgumentException();
			}
		}

		@Override
		public Tags getTags() {
			return new Tags();
		}

		@Override
		public synchronized String getName() {
			if (name == null) {
				StringBuilder sb = new StringBuilder(timers[0].getName());
				for (int i = 1; i < timers.length; i++) {
					sb.append(timers[i].getName());
				}
				name = sb.toString();
			}
			return name;
		}

		@Override
		public TimeKeeping time() {
			return new MultipleTimeKeepingImpl();
		}

		@Override
		public void time(long value) {
			for (Timer t : timers) {
				t.time(value);
			}
		}

		private class MultipleTimeKeepingImpl implements TimeKeeping {

			private long startTime;

			private MultipleTimeKeepingImpl() {
				startTime = clock.millis();
			}

			@Override
			public void close() {
				long elapsed = clock.millis() - startTime;
				for (Timer t : MultipleTimerImpl.this.timers) {
					t.time(elapsed);
				}
			}
		}
	}

	private class TimerImpl implements Timer {

		private final String name;
		private final Tags tags;

		private TimerImpl(String name, Tags tags) {
			this.name = name;
			this.tags = tags;
		}

		@Override
		public Tags getTags() {
			return tags;
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
		private final Tags tags;

		public TimeMeasureImpl(String name, Tags tags) {
			this.name = name;
			this.tags = tags;
		}

		@Override
		public Tags getTags() {
			return tags;
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
		private final Tags tags;

		public HistogramMeasureImpl(String name, Tags tags) {
			this.name = name;
			this.tags = tags;
		}

		@Override
		public Tags getTags() {
			return tags;
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
		public void close() {
			executor.shutdownNow();
		}

		@Override
		public void addGauge(Gauge gauge, Supplier<Long> producer, long time, TimeUnit unit) {
			executor.scheduleAtFixedRate(() -> {
				Long value = producer.get();
				if (value != null) {
					gauge.value(value);
				}
			} , 0, time, unit);
		}

	}

	@Override
	public Metrics batch() {
		return new MetricsImpl(client.batch(), clock, measureAsTime, Optional.of(tags));
	}

	@Override
	public Metrics tagged(CharSequence name, CharSequence value) {
		return new MetricsImpl(client, clock, measureAsTime, Optional.of(tags.with(name, value)));
	}

}
