package com.dianping.puma.pumaserver.handler;

import com.dianping.puma.core.constant.SubscribeConstant;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.netty.entity.BinlogAck;
import com.dianping.puma.core.netty.entity.BinlogSubscription;
import com.dianping.puma.core.netty.entity.BinlogTarget;
import com.dianping.puma.pumaserver.AttributeKeys;
import com.dianping.puma.pumaserver.channel.BinlogChannel;
import com.dianping.puma.pumaserver.channel.impl.ConstantBinlogChannel;
import com.dianping.puma.pumaserver.service.BinlogAckService;
import com.dianping.puma.pumaserver.service.BinlogTargetService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class BinlogSubscriptionHandler extends SimpleChannelInboundHandler<BinlogSubscription> {

	private BinlogTargetService binlogTargetService;

	private BinlogAckService binlogAckService;

	public BinlogSubscriptionHandler(BinlogTargetService binlogTargetService, BinlogAckService binlogAckService) {
		this.binlogTargetService = binlogTargetService;
		this.binlogAckService = binlogAckService;
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, BinlogSubscription binlogSubscription) {
		String clientName = binlogSubscription.getClientName();
		ctx.channel().attr(AttributeKeys.CLIENT_NAME).set(clientName);

		BinlogTarget binlogTarget = binlogTargetService.find(clientName);
		BinlogAck binlogAck = binlogAckService.load(clientName);

		BinlogChannel binlogChannel = buildBinlogChannel(
				binlogTarget.getTargetName(),
				binlogTarget.getDbServerId(),
				null,
				binlogAck.getBinlogInfo(),
				0
		);
		ctx.channel().attr(AttributeKeys.CLIENT_CHANNEL).set(binlogChannel);
	}

	private BinlogChannel buildBinlogChannel(String targetName, long dbServerId, SubscribeConstant sc, BinlogInfo binlogInfo, long timestamp) {
		BinlogChannel binlogChannel = new ConstantBinlogChannel();
		binlogChannel.locate(targetName, dbServerId, sc, binlogInfo, timestamp);

		return binlogChannel;
	}
}
