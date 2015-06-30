package com.dianping.puma.api.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ConnectedHandler extends ChannelInboundHandlerAdapter {

	private volatile boolean connected = false;

	private volatile Channel channel;

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		connected = true;
		channel = ctx.channel();

		super.channelActive(ctx);
	}

	public boolean isConnected() {
		return connected;
	}

	public Channel getChannel() {
		return channel;
	}
}
