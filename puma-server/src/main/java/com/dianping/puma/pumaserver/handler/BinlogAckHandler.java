package com.dianping.puma.pumaserver.handler;

import com.dianping.puma.core.netty.entity.BinlogAck;
import com.dianping.puma.pumaserver.ack.BinlogAckService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class BinlogAckHandler extends SimpleChannelInboundHandler<BinlogAck> {

	private BinlogAckService binlogAckService;

	public BinlogAckHandler(BinlogAckService binlogAckService) {
		this.binlogAckService = binlogAckService;
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, BinlogAck binlogAck) {
		binlogAckService.save(binlogAck.getClientName(), binlogAck.getBinlogInfo());
	}
}
