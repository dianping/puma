package com.dianping.puma.pumaserver.handler.binlog;

import com.dianping.puma.core.dto.BinlogAck;
import com.dianping.puma.core.dto.binlog.request.BinlogRollbackRequest;
import com.dianping.puma.core.dto.binlog.response.BinlogRollbackResponse;
import com.dianping.puma.pumaserver.client.ClientSession;
import com.dianping.puma.pumaserver.service.BinlogAckService;
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

    private BinlogAckService binlogAckService;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BinlogRollbackRequest msg) throws Exception {
        BinlogAck oldAck = binlogAckService.load(msg.getClientName());
        binlogAckService.checkAck(msg.getClientName(), oldAck);

        BinlogAck ack = new BinlogAck();
        ack.setBinlogInfo(msg.getBinlogRollback().getBinlogInfo());
        binlogAckService.save(msg.getClientName(), ack, true);

        ClientSession session = clientSessionService.get(msg.getClientName(), msg.getToken());
        if (session != null) {
            clientSessionService.unsubscribe(msg.getClientName());
        }

        BinlogRollbackResponse response = new BinlogRollbackResponse();
        ctx.channel().writeAndFlush(response);
    }

    public void setClientSessionService(ClientSessionService clientSessionService) {
        this.clientSessionService = clientSessionService;
    }

    public void setBinlogAckService(BinlogAckService binlogAckService) {
        this.binlogAckService = binlogAckService;
    }
}
