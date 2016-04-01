package com.dianping.puma.pumaserver;

import com.dianping.puma.consumer.ha.PumaClientCleaner;
import com.dianping.puma.consumer.intercept.ChainedMessageInterceptor;
import com.dianping.puma.consumer.manage.PumaClientMetaManager;
import com.dianping.puma.pumaserver.client.PumaClientsHolder;
import com.dianping.puma.pumaserver.handler.*;
import com.dianping.puma.pumaserver.handler.binlog.*;
import com.dianping.puma.pumaserver.server.ServerConfig;
import com.dianping.puma.pumaserver.server.TcpServer;
import com.dianping.puma.pumaserver.service.ClientSessionService;
import com.dianping.puma.pumaserver.service.impl.DbBinlogAckService;
import com.dianping.puma.pumaserver.service.impl.DefaultClientSessionService;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    protected DbBinlogAckService binlogAckService;

    @Autowired
    PumaClientMetaManager pumaClientMetaManager;

    @Autowired
    PumaClientCleaner pumaClientCleaner;

    @Autowired
    private ChainedMessageInterceptor pumaEventServerInterceptor;

    protected final ClientSessionService clientSessionService = new DefaultClientSessionService();

    protected final ChannelHolderHandler channelHolderHandler = new ChannelHolderHandler(new PumaClientsHolder());

    @PostConstruct
    public synchronized void init() {
        clientSessionService.init();
        pumaClientCleaner.start();
        pumaClientMetaManager.start();

        ServerConfig consoleConfig = new ServerConfig();
        consoleConfig.setPort(4040);

        // Initialize sharable handlers.
        final InterceptorHandler interceptorHandler = new InterceptorHandler();
        pumaEventServerInterceptor.start();
        interceptorHandler.setPumaInterceptor(pumaEventServerInterceptor);

        final BinlogRollbackHandler binlogRollbackHandler = new BinlogRollbackHandler();
        binlogRollbackHandler.setClientSessionService(clientSessionService);
        binlogRollbackHandler.setBinlogAckService(binlogAckService);

        final BinlogAckHandler binlogAckHandler = new BinlogAckHandler();
        binlogAckHandler.setBinlogAckService(binlogAckService);
        binlogAckHandler.setClientSessionService(clientSessionService);

        final BinlogGetHandler binlogGetHandler = new BinlogGetHandler();
        binlogGetHandler.setClientSessionService(clientSessionService);

        final BinlogSubscriptionHandler binlogSubscriptionHandler = new BinlogSubscriptionHandler();
        binlogSubscriptionHandler.setBinlogAckService(binlogAckService);
        binlogSubscriptionHandler.setClientSessionService(clientSessionService);
        binlogSubscriptionHandler.setPumaClientCleaner(pumaClientCleaner);
        binlogSubscriptionHandler.setPumaClientMetaManager(pumaClientMetaManager);

        final BinlogUnsubscriptionHandler binlogUnsubscriptionHandler = new BinlogUnsubscriptionHandler();
        binlogUnsubscriptionHandler.setClientSessionService(clientSessionService);

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
                result.put("InterceptorHandler", interceptorHandler);
                result.put("BinlogSubscriptionHandler", binlogSubscriptionHandler);
                result.put("BinlogUnsubscriptionHandler", binlogUnsubscriptionHandler);
                result.put("BinlogQueryHandler", binlogGetHandler);
                result.put("BinlogAckHandler", binlogAckHandler);
                result.put("BinlogRollbackHandler", binlogRollbackHandler);
                result.put("ExceptionHandler", ExceptionHandler.INSTANCE);
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
            pumaClientCleaner.stop();
            pumaClientMetaManager.stop();
            pumaEventServerInterceptor.stop();
        }
    }
}
