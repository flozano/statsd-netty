package com.flozano.metrics;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

public final class Tags {
	private static final Tags EMPTY = new Tags(Collections.emptySet());
	private final Set<Tag> tags;

	public static Tags empty() {
		return EMPTY;
	}

	private Tags(Set<Tag> tags) {
		this.tags = tags;
	}

	public final Tags with(CharSequence name, CharSequence value) {
		Set<Tag> t = new TreeSet<Tag>(tags);
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

		public Tag(CharSequence name, CharSequence value) {
			super();
			this.name = requireNonNull(name);
			this.value = requireNonNull(value);
		}

		@Override
		public int compareTo(Tag o) {
			if (o == null) {
				return 1;
			} else {
				return name.toString().compareTo(o.toString());
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Tag other = (Tag) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.toString().equals(other.name.toString()))
				return false;
			return true;
		}

	}
}
