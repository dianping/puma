package com.dianping.puma.pumaserver.handler;

import com.dianping.puma.core.netty.entity.BinlogSubscription;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class BinlogSubscriptionHandler extends SimpleChannelInboundHandler<BinlogSubscription> {

	@Override
	public void channelRead0(ChannelHandlerContext ctx, BinlogSubscription binlogSubscription) {

	}
}
