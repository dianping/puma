package com.dianping.puma.pumaserver.handler;

import com.dianping.puma.core.netty.entity.BinlogAck;
import com.dianping.puma.core.netty.entity.EmptyResponse;
import com.dianping.puma.pumaserver.client.ClientSession;
import com.dianping.puma.pumaserver.service.BinlogAckService;
import com.dianping.puma.pumaserver.service.ClientInfoService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class BinlogAckHandler extends SimpleChannelInboundHandler<BinlogAck> {

    private final BinlogAckService binlogAckService;

    private final ClientInfoService clientInfoService;

    public BinlogAckHandler(BinlogAckService binlogAckService, ClientInfoService clientInfoService) {
        this.binlogAckService = binlogAckService;
        this.clientInfoService = clientInfoService;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, BinlogAck binlogAck) {
        ClientSession clientSession = clientInfoService.get(binlogAck.getClientName(), binlogAck.getToken());
        if (clientSession == null) {
            throw new RuntimeException("must subscribe before binlog ack.");
        }

        final String clientName = clientSession.getClientName();
        if (clientName == null) {
            throw new NullPointerException("null client name.");
        }

        binlogAckService.save(clientName, binlogAck);

        // For browser user.
        ctx.channel().writeAndFlush(new EmptyResponse());
    }
}
