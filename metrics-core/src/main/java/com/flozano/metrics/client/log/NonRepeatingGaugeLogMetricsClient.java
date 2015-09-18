package com.flozano.metrics.client.log;

import static java.util.Objects.requireNonNull;

import java.util.function.BiFunction;

import org.slf4j.Logger;

import com.flozano.metrics.client.GaugeValue;
import com.flozano.metrics.client.MetricValue;

public class NonRepeatingGaugeLogMetricsClient extends LogMetricsClient {
	private BiFunction<String, String, String> valuesHolder;

	public NonRepeatingGaugeLogMetricsClient(Logger logger, BiFunction<String, String, String> valuesHolder) {
		super(logger);
		this.valuesHolder = requireNonNull(valuesHolder);
	}

	@Override
	protected void apply(MetricValue m, String key, String value) {
		assert value != null;
		if ((m instanceof GaugeValue) && skipLog(key, value)) {
			return;
		}
		super.apply(m, key, value);

	}

	private boolean skipLog(String key, String value) {
		return value.equals(valuesHolder.apply(key, value));
	}

}
