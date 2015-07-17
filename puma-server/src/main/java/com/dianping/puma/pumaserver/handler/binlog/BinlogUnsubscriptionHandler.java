package com.dianping.puma.pumaserver.handler.binlog;

import com.dianping.puma.core.dto.binlog.request.BinlogUnsubscriptionRequest;
import com.dianping.puma.core.dto.binlog.response.BinlogUnsubscriptionResponse;
import com.dianping.puma.pumaserver.client.ClientSession;
import com.dianping.puma.pumaserver.service.ClientSessionService;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class BinlogUnsubscriptionHandler extends SimpleChannelInboundHandler<BinlogUnsubscriptionRequest> {

    private ClientSessionService clientSessionService;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, BinlogUnsubscriptionRequest binlogUnsubscriptionRequest) {
        ClientSession session = clientSessionService
                .get(binlogUnsubscriptionRequest.getClientName(), binlogUnsubscriptionRequest.getToken());

        // Destroy binlog channel.
        session.getAsyncBinlogChannel().destroy();

        BinlogUnsubscriptionResponse response = new BinlogUnsubscriptionResponse();
        ctx.channel().writeAndFlush(response);
    }

    public void setClientSessionService(ClientSessionService clientSessionService) {
        this.clientSessionService = clientSessionService;
    }
}
