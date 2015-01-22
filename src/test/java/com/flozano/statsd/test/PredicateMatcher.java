package com.flozano.statsd.test;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.Predicate;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class PredicateMatcher<T> extends BaseMatcher<T> {

	private final Predicate<T> check;
	private final String describe;

	public PredicateMatcher(Predicate<T> check) {
		this(check, null);
	}

	public PredicateMatcher(Predicate<T> check, String describe) {
		this.check = requireNonNull(check);
		this.describe = Optional.ofNullable(describe).orElse(
				"Argument doesn't match predicate");
	}

	@Override
	public void describeTo(Description description) {
		description.appendText(describe);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean matches(Object item) {
		return check.test((T) item);
	}

	public static <T> Matcher<T> compliesWith(Predicate<T> check,
			String describe) {
		return new PredicateMatcher<T>(check, describe);
	}

	public static <T> Matcher<T> compliesWith(Predicate<T> check) {
		return compliesWith(check, null);
	}
}
