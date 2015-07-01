package com.dianping.puma.api.handler;

import com.dianping.puma.core.netty.entity.BinlogMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class BinlogEventHandler extends SimpleChannelInboundHandler<BinlogMessage> {

	@Override
	public void channelRead0(ChannelHandlerContext ctx, BinlogMessage binlogMessage) {

	}
}
