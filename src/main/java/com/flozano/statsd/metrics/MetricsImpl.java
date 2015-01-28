package com.flozano.statsd.metrics;

import static java.util.Objects.requireNonNull;

import java.time.Clock;

import com.flozano.statsd.client.StatsDClient;
import com.flozano.statsd.values.CountValue;
import com.flozano.statsd.values.GaugeValue;
import com.flozano.statsd.values.TimingValue;

final class MetricsImpl implements AutoCloseable, Metrics {

	private final StatsDClient client;
	private final Clock clock;

	public MetricsImpl(StatsDClient client) {
		this(client, Clock.systemUTC());
	}

	public MetricsImpl(StatsDClient client, Clock clock) {
		this.client = requireNonNull(client);
		this.clock = requireNonNull(clock);
	}

	@Override
	public TimerImpl timer(CharSequence... name) {
		return new TimerImpl(metricName(name));
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

		@Override
		public void hit() {
			count(1l);
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
				client.send(new TimingValue(name, elapsed));
			}
		}
	}

	@Override
	public Metrics batch() {
		return new MetricsImpl(client.batch(), clock);
	}

}
