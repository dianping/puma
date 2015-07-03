package com.dianping.puma.pumaserver.handler;

import com.dianping.puma.core.netty.entity.BinlogAck;
import com.dianping.puma.core.netty.entity.response.BinlogAckResponse;
import com.dianping.puma.pumaserver.client.ClientSession;
import com.dianping.puma.pumaserver.client.ClientType;
import com.dianping.puma.pumaserver.service.BinlogAckService;
import com.dianping.puma.pumaserver.service.ClientSessionService;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class BinlogAckHandler extends SimpleChannelInboundHandler<BinlogAck> {

    private BinlogAckService binlogAckService;
    private ClientSessionService clientSessionService;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, BinlogAck binlogAck) {
        ClientSession session = clientSessionService.get(binlogAck.getClientName(), binlogAck.getToken());

        binlogAckService.save(session.getClientName(), binlogAck);

        // For browser user only.
        if (session.getClientType().equals(ClientType.BROSWER)) {
            BinlogAckResponse response = new BinlogAckResponse();
            response.setClientName(session.getClientName());
            response.setToken(session.getToken());
            response.setMsg("ack success");
            ctx.channel().writeAndFlush(response);
        }
    }

    public void setBinlogAckService(BinlogAckService binlogAckService) {
        this.binlogAckService = binlogAckService;
    }

    public void setClientSessionService(ClientSessionService clientSessionService) {
        this.clientSessionService = clientSessionService;
    }
}
