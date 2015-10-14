package com.dianping.puma.pumaserver.handler.binlog;

import com.dianping.puma.core.dto.binlog.request.BinlogRollbackRequest;
import com.dianping.puma.core.dto.binlog.response.BinlogRollbackResponse;
import com.dianping.puma.eventbus.DefaultEventBus;
import com.dianping.puma.eventbus.event.ClientPositionChangedEvent;
import com.dianping.puma.pumaserver.client.ClientSession;
import com.dianping.puma.pumaserver.service.ClientSessionService;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Dozer @ 7/15/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */

@ChannelHandler.Sharable
public class BinlogRollbackHandler extends SimpleChannelInboundHandler<BinlogRollbackRequest> {

    private ClientSessionService clientSessionService;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BinlogRollbackRequest msg) throws Exception {
        ClientSession session = clientSessionService.get(msg.getClientName(), msg.getToken());
        ClientPositionChangedEvent event = new ClientPositionChangedEvent();
        event.setClientName(session.getClientName());
        event.setBinlogInfo(msg.getBinlogRollback().getBinlogInfo());
        DefaultEventBus.INSTANCE.post(event);

        BinlogRollbackResponse response = new BinlogRollbackResponse();
        ctx.channel().writeAndFlush(response);
    }

    public void setClientSessionService(ClientSessionService clientSessionService) {
        this.clientSessionService = clientSessionService;
    }
}
