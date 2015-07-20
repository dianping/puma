package com.dianping.puma.pumaserver.handler.binlog;

import com.dianping.puma.core.constant.SubscribeConstant;
import com.dianping.puma.core.dto.BinlogAck;
import com.dianping.puma.core.dto.BinlogTarget;
import com.dianping.puma.core.dto.binlog.request.BinlogSubscriptionRequest;
import com.dianping.puma.core.dto.binlog.response.BinlogSubscriptionResponse;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.pumaserver.channel.AsyncBinlogChannel;
import com.dianping.puma.pumaserver.channel.impl.DefaultAsyncBinlogChannel;
import com.dianping.puma.pumaserver.client.ClientSession;
import com.dianping.puma.pumaserver.client.ClientType;
import com.dianping.puma.pumaserver.service.BinlogAckService;
import com.dianping.puma.pumaserver.service.BinlogTargetService;
import com.dianping.puma.pumaserver.service.ClientSessionService;
import com.dianping.puma.server.container.TaskContainer;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

@ChannelHandler.Sharable
public class BinlogSubscriptionHandler extends SimpleChannelInboundHandler<BinlogSubscriptionRequest> {

    private BinlogTargetService binlogTargetService;

    private BinlogAckService binlogAckService;

    private ClientSessionService clientSessionService;

    private TaskContainer taskContainer;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, BinlogSubscriptionRequest binlogSubscriptionRequest) {
        String clientName = binlogSubscriptionRequest.getClientName();

        BinlogTarget binlogTarget = binlogTargetService.find(clientName);
        BinlogAck binlogAck = binlogAckService.load(clientName);

        AsyncBinlogChannel asyncBinlogChannel = buildBinlogChannel(
                binlogTarget == null ? null : binlogTarget.getTargetName(),
                binlogAck == null ? SubscribeConstant.SEQ_FROM_LATEST : SubscribeConstant.SEQ_FROM_BINLOGINFO,
                binlogAck == null ? null : binlogAck.getBinlogInfo(),
                0,
                binlogSubscriptionRequest.getDatabase(),
                binlogSubscriptionRequest.getTables(),
                true,
                true,
                true
        );

        ClientSession session = new ClientSession(clientName, asyncBinlogChannel, ClientType.UNKNOW);
        clientSessionService.subscribe(session);

        BinlogSubscriptionResponse binlogSubscriptionResponse = new BinlogSubscriptionResponse();
        binlogSubscriptionResponse.setToken(session.getToken());
        ctx.channel().writeAndFlush(binlogSubscriptionResponse);
    }

    private AsyncBinlogChannel buildBinlogChannel(
            String targetName,
            long sc,
            BinlogInfo binlogInfo,
            long timestamp,
            String database,
            List<String> tables,
            boolean dml,
            boolean ddl,
            boolean transaction) {
        DefaultAsyncBinlogChannel defaultAsyncBinlogChannel = new DefaultAsyncBinlogChannel();
        defaultAsyncBinlogChannel.setTaskContainer(taskContainer);
        defaultAsyncBinlogChannel.init(targetName, sc, binlogInfo, timestamp, database, tables, dml, ddl, transaction);

        return defaultAsyncBinlogChannel;
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

    public void setTaskContainer(TaskContainer taskContainer) {
        this.taskContainer = taskContainer;
    }
}
