package com.flozano.metrics;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

public final class Tags {
	private static final Tags EMPTY = new Tags(Collections.emptySortedSet());
	private final SortedSet<Tag> tags;
	private final int hashCode;

	public static Tags empty() {
		return EMPTY;
	}

	private Tags(SortedSet<Tag> tags) {
		this.tags = tags;
		this.hashCode = tags.hashCode();
	}

	public final Tags with(CharSequence name, CharSequence value) {
		SortedSet<Tag> t = new TreeSet<Tag>(tags);
		t.add(new Tag(name, value));
		return new Tags(t);
	}

	public final Stream<Tag> stream() {
		return tags.stream();
	}

	public boolean isEmpty() {
		return tags.isEmpty();
	}

	public static final class Tag implements Comparable<Tag> {
		public final CharSequence name;
		public final CharSequence value;
		private final int hashCode;

		public Tag(CharSequence name, CharSequence value) {
			super();
			this.name = requireNonNull(name);
			this.value = requireNonNull(value);
			this.hashCode = Objects.hash(name, value);
		}

		@Override
		public int compareTo(Tag o) {
			if (o == null) {
				return 1;
			} else {
				int result = name.toString().compareTo(o.name.toString());
				if (result == 0) {
					return value.toString().compareTo(o.value.toString());
				} else {
					return result;
				}
			}
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof Tag)) {
				return false;
			}
			Tag other = (Tag) obj;
			return Objects.equals(name, other.name) && Objects.equals(value, other.value);
		}

	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Tags)) {
			return false;
		}
		Tags other = (Tags) obj;
		return Objects.equals(tags, other.tags);
	}

	public boolean has(CharSequence name) {
		return tags.stream().anyMatch(x -> x.name.toString().equals(name.toString()));
	}
}
