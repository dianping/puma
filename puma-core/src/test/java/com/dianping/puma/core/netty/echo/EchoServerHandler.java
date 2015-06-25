package com.dianping.puma.core.netty.echo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class EchoServerHandler extends SimpleChannelInboundHandler<String> {

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		System.out.println(String.format("[%s] channelActive: %s", this.getClass(), ctx.channel().remoteAddress()));
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, String msg) {
		System.out.println(String.format("[%s] channelRead: %s", this.getClass(), msg));
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		System.out.println(String.format("[%s] channelReadComplete", this.getClass()));
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		System.out.println(String.format("[%s] exceptionCaught", this.getClass()));
		cause.printStackTrace();
		ctx.close();
	}
}
