package com.dianping.puma.pumaserver.handler;

import com.dianping.puma.core.netty.entity.BinlogQuery;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class BinlogQueryHandler extends SimpleChannelInboundHandler<BinlogQuery> {

	@Override
	public void channelRead0(ChannelHandlerContext ctx, BinlogQuery binlogQuery) {
	}
}
