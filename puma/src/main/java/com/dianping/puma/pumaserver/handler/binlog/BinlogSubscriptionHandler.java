package com.dianping.puma.pumaserver.handler.binlog;

import com.dianping.cat.Cat;
import com.dianping.puma.biz.model.ClientConfig;
import com.dianping.puma.biz.model.ClientConnect;
import com.dianping.puma.core.dto.BinlogAck;
import com.dianping.puma.core.dto.binlog.request.BinlogSubscriptionRequest;
import com.dianping.puma.core.dto.binlog.response.BinlogSubscriptionResponse;
import com.dianping.puma.core.util.AddressUtils;
import com.dianping.puma.pumaserver.channel.impl.DefaultAsyncBinlogChannel;
import com.dianping.puma.pumaserver.client.ClientManager;
import com.dianping.puma.pumaserver.client.ClientSession;
import com.dianping.puma.pumaserver.service.BinlogAckService;
import com.dianping.puma.pumaserver.service.ClientSessionService;
import com.dianping.puma.status.SystemStatusManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@ChannelHandler.Sharable
public class BinlogSubscriptionHandler extends SimpleChannelInboundHandler<BinlogSubscriptionRequest> {

    private BinlogAckService binlogAckService;

    private ClientSessionService clientSessionService;

    private ClientManager clientManager;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, BinlogSubscriptionRequest binlogSubscriptionRequest) {
        String clientName = binlogSubscriptionRequest.getClientName();
        Cat.logEvent("Client.Subscription", String.format("%s %s", clientName, ctx.channel().remoteAddress().toString()));

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setDatabaseName(binlogSubscriptionRequest.getDatabase());
        clientConfig.setTableRegex(generateTableRegex(binlogSubscriptionRequest.getTables()));
        clientConfig.setDml(binlogSubscriptionRequest.isDml());
        clientConfig.setDdl(binlogSubscriptionRequest.isDdl());
        clientManager.addClientConfig(clientName, clientConfig);

        ClientConnect clientConnect = new ClientConnect();
        String clientAddress = ctx.channel().remoteAddress().toString();
        if (StringUtils.startsWith(clientAddress, "/")) {
            clientAddress = StringUtils.substring(clientAddress, 1);
        }
        clientConnect.setClientAddress(clientAddress);
        clientConnect.setServerAddress(AddressUtils.getHostIp());
        clientManager.addClientConnect(clientName, clientConnect);

        BinlogAck binlogAck = binlogAckService.load(clientName);

        binlogAckService.checkAck(clientName,binlogAck);

        DefaultAsyncBinlogChannel defaultAsyncBinlogChannel = new DefaultAsyncBinlogChannel(clientName);
        defaultAsyncBinlogChannel.init(
                binlogAck == null ? null : binlogAck.getBinlogInfo(),
                binlogSubscriptionRequest.getDatabase(),
                binlogSubscriptionRequest.getTables(),
                binlogSubscriptionRequest.isDml(),
                binlogSubscriptionRequest.isDdl(),
                binlogSubscriptionRequest.isTransaction()
        );

        ClientSession session = new ClientSession(clientName, defaultAsyncBinlogChannel, binlogSubscriptionRequest.getCodec());
        clientSessionService.subscribe(session);

        BinlogSubscriptionResponse binlogSubscriptionResponse = new BinlogSubscriptionResponse();
        binlogSubscriptionResponse.setToken(session.getToken());
        ctx.channel().writeAndFlush(binlogSubscriptionResponse);

        SystemStatusManager.addClient(
                clientName,
                ctx.channel().remoteAddress().toString(),
                binlogSubscriptionRequest.getDatabase(),
                binlogSubscriptionRequest.getTables(),
                binlogSubscriptionRequest.isDml(),
                binlogSubscriptionRequest.isDdl(),
                binlogSubscriptionRequest.isTransaction(),
                binlogSubscriptionRequest.getCodec()
        );
    }

    private String generateTableRegex(List<String> tables) {
        return "/" + StringUtils.join(tables, "|") + "/g";
    }

    public void setBinlogAckService(BinlogAckService binlogAckService) {
        this.binlogAckService = binlogAckService;
    }

    public void setClientSessionService(ClientSessionService clientSessionService) {
        this.clientSessionService = clientSessionService;
    }

    public void setClientManager(ClientManager clientManager) {
        this.clientManager = clientManager;
    }
}
