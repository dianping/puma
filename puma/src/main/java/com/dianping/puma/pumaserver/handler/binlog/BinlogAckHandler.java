package com.dianping.puma.pumaserver.handler.binlog;

import com.dianping.puma.common.model.ClientAck;
import com.dianping.puma.core.dto.BinlogAck;
import com.dianping.puma.core.dto.binlog.request.BinlogAckRequest;
import com.dianping.puma.core.dto.binlog.response.BinlogAckResponse;
import com.dianping.puma.pumaserver.client.ClientSession;
import com.dianping.puma.pumaserver.service.BinlogAckService;
import com.dianping.puma.pumaserver.service.ClientSessionService;
import com.dianping.puma.status.SystemStatusManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class BinlogAckHandler extends SimpleChannelInboundHandler<BinlogAckRequest> {

    private BinlogAckService binlogAckService;

    private ClientSessionService clientSessionService;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, BinlogAckRequest binlogAckRequest) {
        ClientSession session = clientSessionService.get(binlogAckRequest.getClientName(), binlogAckRequest.getToken());

        String clientName = session.getClientName();

        ClientAck clientAck = new ClientAck();
        BinlogAck binlogAck = binlogAckRequest.getBinlogAck();
        clientAck.setServerId(binlogAck.getBinlogInfo().getServerId());
        clientAck.setFilename(binlogAck.getBinlogInfo().getBinlogFile());
        clientAck.setPosition(binlogAck.getBinlogInfo().getBinlogPosition());
        clientAck.setTimestamp(binlogAck.getBinlogInfo().getTimestamp());

        binlogAckService.save(session.getClientName(), binlogAckRequest.getBinlogAck(), false);

        BinlogAckResponse response = new BinlogAckResponse();
        ctx.channel().writeAndFlush(response);

        SystemStatusManager.updateClientAckBinlogInfo(binlogAckRequest.getClientName(), binlogAckRequest.getBinlogAck().getBinlogInfo());
    }

    public void setBinlogAckService(BinlogAckService binlogAckService) {
        this.binlogAckService = binlogAckService;
    }

    public void setClientSessionService(ClientSessionService clientSessionService) {
        this.clientSessionService = clientSessionService;
    }
}
