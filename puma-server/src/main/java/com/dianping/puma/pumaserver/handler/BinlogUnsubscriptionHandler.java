package com.dianping.puma.pumaserver.handler;

import com.dianping.puma.core.netty.entity.BinlogUnsubscription;
import com.dianping.puma.pumaserver.AttributeKeys;
import com.dianping.puma.pumaserver.channel.BinlogChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class BinlogUnsubscriptionHandler extends SimpleChannelInboundHandler<BinlogUnsubscription> {

	@Override
	public void channelRead0(ChannelHandlerContext ctx, BinlogUnsubscription binlogUnsubscription) {
		Boolean subscribed = ctx.channel().attr(AttributeKeys.CLIENT_SUBSCRIBED).get();
		if (subscribed == null) {
			throw new RuntimeException("must subscribe before unsubscribe.");
		}

		String clientName = ctx.channel().attr(AttributeKeys.CLIENT_NAME).get();
		if (clientName == null) {
			throw new NullPointerException("null client name.");
		}

		BinlogChannel binlogChannel = ctx.channel().attr(AttributeKeys.CLIENT_CHANNEL).get();
		if (binlogChannel == null) {
			throw new NullPointerException("null binlog channel.");
		}

		binlogChannel.destroy();

		ctx.channel().attr(AttributeKeys.CLIENT_NAME).remove();
		ctx.channel().attr(AttributeKeys.CLIENT_CHANNEL).remove();
		ctx.channel().attr(AttributeKeys.CLIENT_SUBSCRIBED).remove();

		// For browser user.
		ctx.channel().writeAndFlush("unsubscribe success.");
	}
}
