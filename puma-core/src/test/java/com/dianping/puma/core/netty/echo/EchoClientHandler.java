package com.dianping.puma.core.netty.echo;

import io.netty.channel.*;

public class EchoClientHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		ctx.channel().writeAndFlush("hello world\n");
	}
}
