package com.dianping.puma.pumaserver.handler.binlog;

import com.dianping.puma.core.dto.BinlogMessage;
import com.dianping.puma.core.dto.binlog.request.BinlogGetRequest;
import com.dianping.puma.core.dto.binlog.response.BinlogGetResponse;
import com.dianping.puma.pumaserver.client.ClientSession;
import com.dianping.puma.pumaserver.service.ClientSessionService;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;

@ChannelHandler.Sharable
public class BinlogGetHandler extends SimpleChannelInboundHandler<BinlogGetRequest> {

    private ClientSessionService clientSessionService;

    private final BinlogGetResponse EMPTY_RESPONSE = new BinlogGetResponse().setBinlogMessage(new BinlogMessage());

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, final BinlogGetRequest binlogGetRequest) throws IOException {
        binlogGetRequest.setChannel(ctx.channel());
        binlogGetRequest.setStartTime(System.currentTimeMillis());

        final ClientSession session = clientSessionService.get(binlogGetRequest.getClientName(), binlogGetRequest.getToken());
        binlogGetRequest.setCodec(session.getCodec());
        boolean addSuccess = session.getAsyncBinlogChannel().addRequest(binlogGetRequest);

        if (addSuccess) {
            session.setLastChannel(ctx.channel());
        } else {
            ctx.channel().writeAndFlush(EMPTY_RESPONSE);
        }
    }

    public void setClientSessionService(ClientSessionService clientSessionService) {
        this.clientSessionService = clientSessionService;
    }
}
