package com.flozano.statsd.netty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.flozano.statsd.metrics.Count;
import com.flozano.statsd.metrics.Gauge;
import com.flozano.statsd.metrics.Metric;
import com.flozano.statsd.metrics.Timing;

@RunWith(Parameterized.class)
public class UDPEncoderTest {

	UDPEncoder encoder;

	@Mock
	ChannelHandlerContext ctx;

	private final Metric metric;
	private final String expectedOutput;
	private final String host;
	private final int port;

	public UDPEncoderTest(Metric metric, String expectedOutput, String host,
			int port) {
		this.metric = metric;
		this.expectedOutput = expectedOutput;
		this.host = host;
		this.port = port;
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
		assertThat(out.get(0), IsInstanceOf.instanceOf(DatagramPacket.class));
		DatagramPacket packet = (DatagramPacket) out.get(0);
		InetSocketAddress target = packet.recipient();
		assertEquals(host, target.getHostString());
		assertEquals(port, target.getPort());
		assertEquals(expectedOutput, new String(packet.content().array(),
				StandardCharsets.UTF_8));

	}

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays
				.asList(new Object[][] {
						{ new Timing("timing1", 1234), "timing1:1234|ms",
								"1.2.3.4", 10203 },
						{ new Timing("timing2", 1234, 0.5),
								"timing2:1234|ms|@0.5", "1.2.3.5", 10205 },
						{ new Count("counter1", 1234), "counter1:1234|c",
								"1.2.3.4", 10203 },
						{ new Count("counter2", 1234, 0.5),
								"counter2:1234|c|@0.5", "1.2.3.5", 10205 },
						{ new Gauge("gauge1", 1234), "gauge1:1234|g",
								"1.2.3.4", 10203 },
						{ new Gauge("gauge2", 1234, 0.5), "gauge2:1234|g|@0.5",
								"1.2.3.5", 10205 } });
	}
}
