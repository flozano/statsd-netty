package com.flozano.statsd.mock;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class UDPToStringDecoder extends MessageToMessageDecoder<DatagramPacket> {

	@Override
	protected void decode(ChannelHandlerContext ctx, DatagramPacket msg,
			List<Object> out) throws Exception {
		out.add(msg.content().toString(StandardCharsets.UTF_8));

	}

}
