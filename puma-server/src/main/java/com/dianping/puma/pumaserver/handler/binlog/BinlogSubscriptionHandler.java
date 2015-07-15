package com.dianping.puma.pumaserver.handler.binlog;

import com.dianping.puma.core.constant.SubscribeConstant;
import com.dianping.puma.core.dto.BinlogAck;
import com.dianping.puma.core.dto.BinlogTarget;
import com.dianping.puma.core.dto.binlog.request.BinlogSubscriptionRequest;
import com.dianping.puma.core.dto.binlog.response.BinlogSubscriptionResponse;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.pumaserver.channel.BinlogChannel;
import com.dianping.puma.pumaserver.channel.impl.ConstantBinlogChannel;
import com.dianping.puma.pumaserver.client.ClientSession;
import com.dianping.puma.pumaserver.client.ClientType;
import com.dianping.puma.pumaserver.service.BinlogAckService;
import com.dianping.puma.pumaserver.service.BinlogTargetService;
import com.dianping.puma.pumaserver.service.ClientSessionService;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class BinlogSubscriptionHandler extends SimpleChannelInboundHandler<BinlogSubscriptionRequest> {

    private BinlogTargetService binlogTargetService;
    private BinlogAckService binlogAckService;
    private ClientSessionService clientSessionService;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, BinlogSubscriptionRequest binlogSubscriptionRequest) {
        String clientName = binlogSubscriptionRequest.getClientName();

        BinlogTarget binlogTarget = binlogTargetService.find(clientName);
        BinlogAck binlogAck = binlogAckService.load(clientName);

        BinlogChannel binlogChannel = buildBinlogChannel(
                binlogTarget == null ? null : binlogTarget.getTargetName(),
                binlogTarget == null ? 0 : binlogTarget.getDbServerId(),
                null,
                binlogAck == null ? null : binlogAck.getBinlogInfo(),
                0
        );

        ClientSession session = new ClientSession(clientName, binlogChannel, ClientType.BROSWER);
        clientSessionService.subscribe(session);

        BinlogSubscriptionResponse binlogSubscriptionResponse = new BinlogSubscriptionResponse();
        binlogSubscriptionResponse.setToken(session.getToken());
        binlogSubscriptionResponse.setMsg("subscribe success");
        ctx.channel().writeAndFlush(binlogSubscriptionResponse);
    }

    private BinlogChannel buildBinlogChannel(String targetName, long dbServerId, SubscribeConstant sc,
                                             BinlogInfo binlogInfo, long timestamp) {
        BinlogChannel binlogChannel = new ConstantBinlogChannel();
        binlogChannel.locate(targetName, dbServerId, sc, binlogInfo, timestamp);

        return binlogChannel;
    }

    public void setBinlogTargetService(BinlogTargetService binlogTargetService) {
        this.binlogTargetService = binlogTargetService;
    }

    public void setBinlogAckService(BinlogAckService binlogAckService) {
        this.binlogAckService = binlogAckService;
    }

    public void setClientSessionService(ClientSessionService clientSessionService) {
        this.clientSessionService = clientSessionService;
    }
}
