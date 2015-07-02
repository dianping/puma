package com.dianping.puma.pumaserver.handler;

import com.dianping.puma.core.netty.entity.BinlogAck;
import com.dianping.puma.pumaserver.AttributeKeys;
import com.dianping.puma.pumaserver.service.BinlogAckService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class BinlogAckHandler extends SimpleChannelInboundHandler<BinlogAck> {

	private BinlogAckService binlogAckService;

	public BinlogAckHandler(BinlogAckService binlogAckService) {
		this.binlogAckService = binlogAckService;
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, BinlogAck binlogAck) {
		Boolean subscribed = ctx.channel().attr(AttributeKeys.CLIENT_SUBSCRIBED).get();
		if (subscribed == null) {
			throw new RuntimeException("must subscribe before binlog ack.");
		}

		final String clientName = ctx.channel().attr(AttributeKeys.CLIENT_NAME).get();
		if (clientName == null) {
			throw new NullPointerException("null client name.");
		}

		binlogAckService.save(clientName, binlogAck);

		// For browser user.
		ctx.channel().writeAndFlush("ack success.");
	}
}
