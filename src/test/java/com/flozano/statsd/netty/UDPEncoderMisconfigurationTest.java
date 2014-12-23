package com.flozano.statsd.netty;

import org.junit.Test;

public class UDPEncoderMisconfigurationTest {

	@Test(expected = NullPointerException.class)
	public void testNullHost() {
		new UDPEncoder(null, 1234);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testWrongPort() {
		new UDPEncoder("1.2.3.4", 65539);
	}

}
