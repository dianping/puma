package com.dianping.puma.pumaserver.handler.binlog;

import com.dianping.puma.core.constant.SubscribeConstant;
import com.dianping.puma.core.dto.BinlogAck;
import com.dianping.puma.core.dto.binlog.request.BinlogSubscriptionRequest;
import com.dianping.puma.core.dto.binlog.response.BinlogSubscriptionResponse;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.pumaserver.channel.AsyncBinlogChannel;
import com.dianping.puma.pumaserver.channel.impl.DefaultAsyncBinlogChannel;
import com.dianping.puma.pumaserver.client.ClientSession;
import com.dianping.puma.pumaserver.client.ClientType;
import com.dianping.puma.pumaserver.service.BinlogAckService;
import com.dianping.puma.pumaserver.service.ClientSessionService;
import com.dianping.puma.server.container.TaskContainer;
import com.dianping.puma.status.SystemStatusManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

@ChannelHandler.Sharable
public class BinlogSubscriptionHandler extends SimpleChannelInboundHandler<BinlogSubscriptionRequest> {

    private BinlogAckService binlogAckService;

    private ClientSessionService clientSessionService;

    private TaskContainer taskContainer;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, BinlogSubscriptionRequest binlogSubscriptionRequest) {
        String clientName = binlogSubscriptionRequest.getClientName();

        BinlogAck binlogAck = binlogAckService.load(clientName);

        AsyncBinlogChannel asyncBinlogChannel = buildBinlogChannel(
                binlogAck == null ? SubscribeConstant.SEQ_FROM_LATEST : SubscribeConstant.SEQ_FROM_BINLOGINFO,
                binlogAck == null ? null : binlogAck.getBinlogInfo(),
                binlogSubscriptionRequest.getDatabase(),
                binlogSubscriptionRequest.getTables(),
                binlogSubscriptionRequest.isDml(),
                binlogSubscriptionRequest.isDdl(),
                binlogSubscriptionRequest.isTransaction()
        );

        ClientSession session = new ClientSession(clientName, asyncBinlogChannel, ClientType.UNKNOW);
        clientSessionService.subscribe(session);

        BinlogSubscriptionResponse binlogSubscriptionResponse = new BinlogSubscriptionResponse();
        binlogSubscriptionResponse.setToken(session.getToken());
        ctx.channel().writeAndFlush(binlogSubscriptionResponse);

        SystemStatusManager.addClient(
                clientName,
                binlogSubscriptionRequest.getDatabase(),
                binlogSubscriptionRequest.getTables(),
                binlogSubscriptionRequest.isDml(),
                binlogSubscriptionRequest.isDdl(),
                binlogSubscriptionRequest.isTransaction(),
                "json"
        );
    }

    private AsyncBinlogChannel buildBinlogChannel(
            long sc,
            BinlogInfo binlogInfo,
            String database,
            List<String> tables,
            boolean dml,
            boolean ddl,
            boolean transaction) {
        DefaultAsyncBinlogChannel defaultAsyncBinlogChannel = new DefaultAsyncBinlogChannel();
        defaultAsyncBinlogChannel.setTaskContainer(taskContainer);
        defaultAsyncBinlogChannel.init(sc, binlogInfo, database, tables, dml, ddl, transaction);

        return defaultAsyncBinlogChannel;
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
