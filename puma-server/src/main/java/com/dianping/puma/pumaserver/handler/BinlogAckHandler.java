package com.dianping.puma.pumaserver.handler;

import com.dianping.puma.core.netty.entity.BinlogAck;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class BinlogAckHandler extends SimpleChannelInboundHandler<BinlogAck> {

	@Override
	public void channelRead0(ChannelHandlerContext ctx, BinlogAck binlogAck) {
	}
}
