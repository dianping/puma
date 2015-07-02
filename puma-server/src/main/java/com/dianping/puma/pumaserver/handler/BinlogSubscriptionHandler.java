package com.dianping.puma.pumaserver.handler;

import com.dianping.puma.core.constant.SubscribeConstant;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.netty.entity.BinlogSubscription;
import com.dianping.puma.pumaserver.channel.BinlogChannel;
import com.dianping.puma.pumaserver.channel.impl.ConstantBinlogChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class BinlogSubscriptionHandler extends SimpleChannelInboundHandler<BinlogSubscription> {

	@Override
	public void channelRead0(ChannelHandlerContext ctx, BinlogSubscription binlogSubscription) {
	}

	private BinlogChannel buildBinlogChannel(String targetName, long dbServerId, SubscribeConstant sc, BinlogInfo binlogInfo, long timestamp) {
		BinlogChannel binlogChannel = new ConstantBinlogChannel();
		binlogChannel.locate(targetName, dbServerId, sc, binlogInfo, timestamp);

		return binlogChannel;
	}
}
