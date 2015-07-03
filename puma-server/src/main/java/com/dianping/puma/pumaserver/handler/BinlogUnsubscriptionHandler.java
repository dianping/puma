package com.dianping.puma.pumaserver.handler;

import com.dianping.puma.core.netty.entity.BinlogUnsubscription;
import com.dianping.puma.core.netty.entity.EmptyResponse;
import com.dianping.puma.pumaserver.service.ClientSessionService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class BinlogUnsubscriptionHandler extends SimpleChannelInboundHandler<BinlogUnsubscription> {

    private final ClientSessionService clientSessionService;

    public BinlogUnsubscriptionHandler(ClientSessionService clientSessionService) {
        this.clientSessionService = clientSessionService;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, BinlogUnsubscription binlogUnsubscription) {
        clientSessionService.unsubscribe(binlogUnsubscription.getClientName(), binlogUnsubscription.getToken());
        ctx.channel().writeAndFlush(new EmptyResponse());
    }
}
