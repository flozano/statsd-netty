package com.flozano.statsd.netty;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.hamcrest.CustomMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.flozano.statsd.metrics.Count;
import com.flozano.statsd.metrics.Metric;

public class MetricArrayToBytesEncoderTest {

	@Mock
	ChannelHandlerContext ctx;

	@Mock
	ByteBufAllocator allocator;

	List<Object> out;

	MetricArrayToBytesEncoder encoder;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		when(ctx.alloc()).thenReturn(allocator);
		when(allocator.buffer()).thenAnswer(invocation -> Unpooled.buffer());
		out = new LinkedList<>();
		encoder = new MetricArrayToBytesEncoder(20);
	}

	@Test
	public void testEmpty() throws Exception {
		encoder.encode(ctx, new Metric[] {}, out);
		assertThat(out, IsEmptyCollection.empty());
		verifyNoMoreInteractions(ctx);
		verifyNoMoreInteractions(allocator);
	}

	@Test
	public void testNull() throws Exception {
		encoder.encode(ctx, null, out);
		assertThat(out, IsEmptyCollection.empty());
		verifyNoMoreInteractions(ctx);
		verifyNoMoreInteractions(allocator);
	}

	@Test
	public void testNullELement() throws Exception {
		encoder.encode(ctx, new Metric[] { null }, out);
		assertThat(out, IsEmptyCollection.empty());
		verifyNoMoreInteractions(ctx);
		verifyNoMoreInteractions(allocator);
	}

	@Test
	public void testSingleElement() throws Exception {
		Metric[] elements = new Metric[] { element(19) };
		encoder.encode(ctx, elements, out);
		assertThat(out, containsBufferFor(elements));
		verify(allocator, times(1)).buffer();
		verify(ctx, times(1)).alloc();
		verifyNoMoreInteractions(ctx, allocator);
	}
	
	@Test
	public void testSingleBigElement() throws Exception {
		Metric[] elements = new Metric[] { element(50) };
		encoder.encode(ctx, elements, out);
		assertThat(out, containsBufferFor(elements));
		verify(allocator, times(1)).buffer();
		verify(ctx, times(1)).alloc();
		verifyNoMoreInteractions(ctx, allocator);
	}

	@Test
	public void testFewElements_fitInOnePackage() throws Exception {
		Metric[] elements = new Metric[] { element(5), element(5), element(5) };
		encoder.encode(ctx, elements, out);
		assertThat(out, containsBufferFor(elements));
		verify(allocator, times(1)).buffer();
		verify(ctx, times(1)).alloc();
		verifyNoMoreInteractions(ctx, allocator);
	}

	@Test
	public void testFewElements_cantFitInOnePackage() throws Exception {
		Metric[] elements = new Metric[] { element(10), element(10),
				element(10) };
		Metric[] elements1 = new Metric[] { elements[0], elements[1] };
		Metric[] elements2 = new Metric[] { elements[2] };

		encoder.encode(ctx, elements, out);
		assertThat(out, containsBufferFor(elements1, elements2));
		verify(allocator, times(2)).buffer();
		verify(ctx, times(2)).alloc();
		verifyNoMoreInteractions(ctx, allocator);
	}

	static Matcher<List<Object>> containsBufferFor(Metric[]... metricsArgs) {
		return new CustomMatcher<List<Object>>(
				"Contains buffers for the specicied metrics") {

			@Override
			public boolean matches(Object item) {
				@SuppressWarnings("unchecked")
				List<String> items = ((List<Object>) item)
						.stream()
						.map((x) -> ((ByteBuf) x)
								.toString(StandardCharsets.UTF_8))
						.collect(Collectors.toList());
				if (metricsArgs.length != items.size()) {
					return false;
				}

				for (Metric[] metrics : metricsArgs) {
					String payload = Arrays.asList(metrics).stream()
							.map((x) -> x.toString())
							.collect(Collectors.joining("\n"));

					if (!items.contains(payload)) {
						return false;
					}
				}
				return true;
			}
		};

	}

	static Metric element(int nBytes) {
		StringBuilder sb = new StringBuilder("a");
		for (;;) {
			Metric m = new Count(sb.toString(), 1, null);
			int length = m.toString().getBytes(StandardCharsets.UTF_8).length;
			if (length < nBytes) {
				sb = sb.append('a');
			} else if (length > nBytes) {
				throw new IllegalArgumentException(
						"Provided amount of bytes too low");
			} else {
				return m;
			}
		}
	}
}
