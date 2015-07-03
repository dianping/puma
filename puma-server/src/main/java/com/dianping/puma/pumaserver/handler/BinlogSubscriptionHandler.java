package com.dianping.puma.pumaserver.handler;

import com.dianping.puma.core.constant.SubscribeConstant;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.netty.entity.BinlogAck;
import com.dianping.puma.core.netty.entity.BinlogSubscription;
import com.dianping.puma.core.netty.entity.BinlogSubscriptionResponse;
import com.dianping.puma.core.netty.entity.BinlogTarget;
import com.dianping.puma.pumaserver.channel.BinlogChannel;
import com.dianping.puma.pumaserver.channel.impl.ConstantBinlogChannel;
import com.dianping.puma.pumaserver.client.ClientSession;
import com.dianping.puma.pumaserver.client.ClientType;
import com.dianping.puma.pumaserver.service.BinlogAckService;
import com.dianping.puma.pumaserver.service.BinlogTargetService;
import com.dianping.puma.pumaserver.service.ClientInfoService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class BinlogSubscriptionHandler extends SimpleChannelInboundHandler<BinlogSubscription> {

    private final BinlogTargetService binlogTargetService;

    private final BinlogAckService binlogAckService;

    private final ClientInfoService clientInfoService;

    public BinlogSubscriptionHandler(BinlogTargetService binlogTargetService, BinlogAckService binlogAckService, ClientInfoService clientInfoService) {
        this.binlogTargetService = binlogTargetService;
        this.binlogAckService = binlogAckService;
        this.clientInfoService = clientInfoService;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, BinlogSubscription binlogSubscription) {
        String clientName = binlogSubscription.getClientName();

        BinlogTarget binlogTarget = binlogTargetService.find(clientName);
        BinlogAck binlogAck = binlogAckService.load(clientName);

        BinlogChannel binlogChannel = buildBinlogChannel(
                binlogTarget == null ? null : binlogTarget.getTargetName(),
                binlogTarget == null ? 0 : binlogTarget.getDbServerId(),
                null,
                binlogAck == null ? null : binlogAck.getBinlogInfo(),
                0
        );

        ClientSession clientSession = new ClientSession(clientName, binlogChannel, ClientType.UNKNOW);

        String token = clientInfoService.subscribe(clientSession);

        // For browser user.
        ctx.channel().writeAndFlush(new BinlogSubscriptionResponse().setToken(token));
    }

    private BinlogChannel buildBinlogChannel(String targetName, long dbServerId, SubscribeConstant sc, BinlogInfo binlogInfo, long timestamp) {
        BinlogChannel binlogChannel = new ConstantBinlogChannel();
        binlogChannel.locate(targetName, dbServerId, sc, binlogInfo, timestamp);

        return binlogChannel;
    }
}
