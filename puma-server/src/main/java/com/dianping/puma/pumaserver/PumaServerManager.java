package com.dianping.puma.pumaserver;

import com.dianping.puma.core.netty.handler.ChannelHolderHandler;
import com.dianping.puma.core.netty.handler.HandlerFactory;
import com.dianping.puma.core.netty.handler.HttpResponseEncoder;
import com.dianping.puma.core.netty.server.ServerConfig;
import com.dianping.puma.core.netty.server.TcpServer;
import com.dianping.puma.pumaserver.client.PumaClientsHolder;
import com.dianping.puma.pumaserver.handler.*;
import com.dianping.puma.pumaserver.service.BinlogAckService;
import com.dianping.puma.pumaserver.service.BinlogTargetService;
import com.dianping.puma.pumaserver.service.ClientSessionService;
import com.dianping.puma.pumaserver.service.impl.CachedBinlogAckService;
import com.dianping.puma.pumaserver.service.impl.DefaultClientSessionService;
import com.dianping.puma.pumaserver.service.impl.LionBinlogTargetService;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Dozer @ 6/24/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
@Component
public class PumaServerManager {
    public volatile static TcpServer server;

    protected final BinlogTargetService binlogTargetService = new LionBinlogTargetService();
    protected final BinlogAckService binlogAckService = new CachedBinlogAckService();
    protected final ClientSessionService clientSessionService = new DefaultClientSessionService();

    protected final ChannelHolderHandler channelHolderHandler = new ChannelHolderHandler(new PumaClientsHolder());

    @PostConstruct
    public synchronized void init() {
        ServerConfig consoleConfig = new ServerConfig();
        consoleConfig.setPort(4040);

        consoleConfig.setHandlerFactory(new HandlerFactory() {
            @Override
            public Map<String, ChannelHandler> getHandlers() {
                Map<String, ChannelHandler> result = new LinkedHashMap<String, ChannelHandler>();
                result.put("channelHolderHandler", channelHolderHandler);
                result.put("HttpRequestDecoder", new HttpRequestDecoder());
                result.put("HttpContentDecompressor", new HttpContentDecompressor());
                result.put("HttpResponseEncoder", new io.netty.handler.codec.http.HttpResponseEncoder());
                result.put("HttpContentCompressor", new HttpContentCompressor());
                result.put("HttpEntityEncoder", HttpResponseEncoder.INSTANCE);
                result.put("HttpObjectAggregator", new HttpObjectAggregator(1024 * 1024 * 32));
                result.put("HttpRouterHandler", HttpRouterHandler.INSTANCE);
                result.put("StatusQueryHandler", StatusQueryHandler.INSTANCE);
                result.put("BinlogSubscriptionHandler", new BinlogSubscriptionHandler(binlogTargetService, binlogAckService, clientSessionService));
                result.put("BinlogUnsubscriptionHandler", new BinlogUnsubscriptionHandler(clientSessionService));
                result.put("BinlogQueryHandler", new BinlogQueryHandler(binlogAckService, clientSessionService));
                result.put("BinlogAckHandler", new BinlogAckHandler(binlogAckService, clientSessionService));
                result.put("DeprecatedBinlogQueryHandler", new DeprecatedBinlogQueryHandler());
                return result;
            }
        });
        server = new TcpServer(consoleConfig);
        server.init();
    }

    @PreDestroy
    public synchronized void close() {
        if (server != null) {
            server.close();
        }
    }
}
