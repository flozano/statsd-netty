package com.flozano.metrics.client.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.flozano.metrics.client.util.NameComposer;

public class NameComposerTest {

	@Test
	public void simple() {
		assertEquals("testing", NameComposer.composeName("testing"));
		assertEquals("testing", NameComposer.composeName("testing", null));
		assertEquals("testing", NameComposer.composeName("testing", null, null));
	}

	@Test(expected = IllegalArgumentException.class)
	public void badNullFirstValue() {
		NameComposer.composeName(null, "something");
	}

	@Test(expected = NullPointerException.class)
	public void badNullArray() {
		NameComposer.composeName((String[]) null);
	}

	@Test
	public void composed() {
		assertEquals("testing.part1.part2",
				NameComposer.composeName("testing", "part1", "part2"));
		assertEquals("testing.part1.part2",
				NameComposer.composeName("testing", "part1", null, "part2"));
	}

}
