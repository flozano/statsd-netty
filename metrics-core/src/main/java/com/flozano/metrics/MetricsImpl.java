package com.flozano.metrics;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import com.flozano.metrics.client.CountValue;
import com.flozano.metrics.client.GaugeValue;
import com.flozano.metrics.client.HistogramValue;
import com.flozano.metrics.client.MetricsClient;
import com.flozano.metrics.client.TimingValue;

final class MetricsImpl implements AutoCloseable, Metrics {

	private final MetricsClient client;
	private final Clock clock;
	private final boolean measureAsTime;
	private final boolean smartGauges;
	private final BackgroundReporter reporter;

	private final Tags tags;

	MetricsImpl(MetricsClient client, Clock clock, boolean measureAsTime, boolean smartGauges, Optional<Tags> tags) {
		this.client = requireNonNull(client);
		this.clock = requireNonNull(clock);
		this.measureAsTime = measureAsTime;
		this.smartGauges = smartGauges;
		this.reporter = new SimpleGaugeReporter();
		this.tags = tags.orElseGet(() -> Tags.empty());
	}

	@Override
	public TimerImpl timer(CharSequence... name) {
		return new TimerImpl(metricName(name));
	}

	@Override
	public Timer multi(Timer... timers) {
		return new MultipleTimerImpl(timers);
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
		if (smartGauges) {
			return new SmartGaugeImpl(metricName(name));
		} else {
			return new GaugeImpl(metricName(name));
		}
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

		private CounterImpl(String name) {
			this.name = name;
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
			client.send(new CountValue(name, value, tags));
		}
	}

	private class GaugeImpl implements Gauge {

		private final String name;

		private GaugeImpl(String name) {
			this.name = name;
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
			client.send(new GaugeValue(name, value, false, tags));
		}

		@Override
		public void delta(long value) {
			if (value != 0) {
				client.send(new GaugeValue(name, value, true, tags));
			}
		}

		@Override
		public void supply(long time, TimeUnit unit, Supplier<Long> supplier) {
			reporter.addGauge(this, supplier, time, unit);
		}

	}

	private class SmartGaugeImpl extends GaugeImpl {

		private AtomicLong lastValue;

		private SmartGaugeImpl(String name) {
			super(name);
		}

		@Override
		public void value(long value) {
			if (lastValue.getAndSet(value) != value) {
				super.value(value);
			}
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
			return tags;
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

		private TimerImpl(String name) {
			this.name = name;
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
			client.send(new TimingValue(name, value, tags));
		}
	}

	private class TimeMeasureImpl implements Measure {

		private final String name;

		private TimeMeasureImpl(String name) {
			this.name = name;
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
			client.send(new TimingValue(name, value, tags));
		}

	}

	private class HistogramMeasureImpl implements Measure {

		private final String name;

		private HistogramMeasureImpl(String name) {
			this.name = name;
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
			client.send(new HistogramValue(name, value, tags));
		}

	}

	private class SimpleGaugeReporter implements BackgroundReporter {

		private final ScheduledExecutorService executor;

		private SimpleGaugeReporter() {
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
		return new MetricsImpl(client.batch(), clock, measureAsTime, smartGauges, Optional.of(tags));
	}

	@Override
	public Metrics tagged(CharSequence name, CharSequence value) {
		return new MetricsImpl(client, clock, measureAsTime, smartGauges, Optional.of(tags.with(name, value)));
	}

}
