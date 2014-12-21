package com.flozano.statsd.netty;

import static org.junit.Assert.assertEquals;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.flozano.statsd.metrics.Count;
import com.flozano.statsd.metrics.Metric;

@RunWith(Parameterized.class)
public class UDPEncoderTest {
	String host = "1.2.3.4";
	int port = 4321;
	UDPEncoder encoder;

	@Mock
	ChannelHandlerContext ctx;

	private Metric metric;
	private String expectedOutput;

	public UDPEncoderTest(Metric metric, String expectedOutput) {
		this.metric = metric;
		this.expectedOutput = expectedOutput;
	}

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		encoder = new UDPEncoder(host, port);
	}

	@Test
	public void encode() throws Exception {
		ArrayList<Object> out = new ArrayList<>(1);
		encoder.encode(ctx, metric, out);
		DatagramPacket packet = (DatagramPacket) out.get(0);
		assertEquals(expectedOutput, new String(packet.content().array(),
				StandardCharsets.UTF_8));

	}

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
				{ new Count("counter1", 1234), "counter1:1234|c" },
				{ new Count("counter2", 1234, 0.5), "counter2:1234|c|@0.5" } });
	}

}
