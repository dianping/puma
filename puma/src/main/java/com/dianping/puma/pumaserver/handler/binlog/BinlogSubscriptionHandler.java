package com.dianping.puma.pumaserver.handler.binlog;

import com.dianping.cat.Cat;
import com.dianping.puma.common.service.PumaClientAckService;
import com.dianping.puma.common.utils.AddressUtils;
import com.dianping.puma.core.dto.BinlogAck;
import com.dianping.puma.core.dto.binlog.request.BinlogSubscriptionRequest;
import com.dianping.puma.core.dto.binlog.response.BinlogSubscriptionResponse;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.pumaserver.channel.impl.DefaultAsyncBinlogChannel;
import com.dianping.puma.pumaserver.client.ClientManager;
import com.dianping.puma.pumaserver.client.ClientSession;
import com.dianping.puma.pumaserver.service.BinlogAckService;
import com.dianping.puma.pumaserver.service.ClientSessionService;
import com.dianping.puma.server.manage.PumaClientMetaManager;
import com.dianping.puma.server.model.ClientConfig;
import com.dianping.puma.server.model.ClientConnect;
import com.dianping.puma.server.model.ClientToken;
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

    private PumaClientMetaManager pumaClientMetaManager;

    private PumaClientAckService clientAckService;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, BinlogSubscriptionRequest binlogSubscriptionRequest) {
        String clientName = binlogSubscriptionRequest.getClientName();
        Cat.logEvent("Client.Subscription", String.format("%s %s", clientName, ctx.channel().remoteAddress().toString()));

        BinlogInfo binlogInfo;
        BinlogAck binlogAck = binlogAckService.load(clientName);
        binlogAckService.checkAck(clientName,binlogAck);
        binlogInfo = (binlogAck == null) ? null : binlogAck.getBinlogInfo();

        DefaultAsyncBinlogChannel defaultAsyncBinlogChannel = new DefaultAsyncBinlogChannel(clientName);
        defaultAsyncBinlogChannel.init(
                binlogInfo,
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

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setDatabaseName(binlogSubscriptionRequest.getDatabase());
        clientConfig.setTableRegex(generateTableRegex(binlogSubscriptionRequest.getTables()));
        clientConfig.setDml(binlogSubscriptionRequest.isDml());
        clientConfig.setDdl(binlogSubscriptionRequest.isDdl());
        pumaClientMetaManager.registerClientConfig(clientName, clientConfig);

        ClientConnect clientConnect = new ClientConnect();
        String clientAddress = ctx.channel().remoteAddress().toString();
        if (StringUtils.startsWith(clientAddress, "/")) {
            clientAddress = StringUtils.substring(clientAddress, 1);
        }
        clientConnect.setClientAddress(clientAddress);
        clientConnect.setServerAddress(AddressUtils.getHostIp());
        pumaClientMetaManager.registerClientConnect(clientName, clientConnect);

        ClientToken clientToken = new ClientToken();
        clientToken.setToken(session.getToken());
        pumaClientMetaManager.registerClientToken(clientName, clientToken);

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

    public void setClientAckService(PumaClientAckService clientAckService) {
        this.clientAckService = clientAckService;
    }

    public void setClientManager(ClientManager clientManager) {
        this.clientManager = clientManager;
    }
}
