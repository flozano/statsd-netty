package com.flozano.statsd.util;

import static java.util.Objects.requireNonNull;

public final class NameComposer {
	private NameComposer() {
		throw new IllegalStateException();
	};

	public static String composeName(String... names) {
		requireNonNull(names);
		if (names.length < 1) {
			throw new IllegalArgumentException("At least a name is required");
		}
		if (names[0] == null) {
			throw new IllegalArgumentException("First element cannot be null");
		}
		if (names.length == 1) {
			return names[0];
		}
		final StringBuilder sb = new StringBuilder(names[0]);
		for (int i = 1; i < names.length; i++) {
			if (names[i] != null) {
				sb.append('.').append(names[i]);
			}
		}
		return sb.toString();
	}
}
