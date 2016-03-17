package com.dianping.puma.pumaserver;

import com.dianping.puma.alarm.log.remote.RemotePullTimeDelayAlarmLogger;
import com.dianping.puma.alarm.log.remote.RemotePushTimeDelayAlarmLogger;
import com.dianping.puma.alarm.service.ClientAlarmDataService;
import com.dianping.puma.common.intercept.ChainedInterceptor;
import com.dianping.puma.common.intercept.PumaInterceptor;
import com.dianping.puma.common.service.ClientAckService;
import com.dianping.puma.core.dto.BinlogHttpMessage;
import com.dianping.puma.pumaserver.client.ClientManager;
import com.dianping.puma.pumaserver.client.PumaClientsHolder;
import com.dianping.puma.pumaserver.handler.*;
import com.dianping.puma.pumaserver.handler.binlog.*;
import com.dianping.puma.pumaserver.server.ServerConfig;
import com.dianping.puma.pumaserver.server.TcpServer;
import com.dianping.puma.pumaserver.service.ClientSessionService;
import com.dianping.puma.pumaserver.service.impl.DbBinlogAckService;
import com.dianping.puma.pumaserver.service.impl.DefaultClientSessionService;
import com.google.common.collect.Lists;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
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

    @Resource(name = "asyncRemoteClientManager")
    ClientManager clientManager;

    @Autowired
    ClientAlarmDataService clientAlarmDataService;

    @Autowired
    ClientAckService clientAckService;

    private ChainedInterceptor<BinlogHttpMessage> chainedInterceptor;

    protected final ClientSessionService clientSessionService = new DefaultClientSessionService();

    protected final ChannelHolderHandler channelHolderHandler = new ChannelHolderHandler(new PumaClientsHolder());

    @PostConstruct
    public synchronized void init() {
        clientSessionService.init();
        clientManager.start();

        ServerConfig consoleConfig = new ServerConfig();
        consoleConfig.setPort(4040);

        // Initialize sharable handlers.
        final InterceptorHandler interceptorHandler = new InterceptorHandler();
        List<PumaInterceptor<BinlogHttpMessage>> interceptors = Lists.newArrayList();

        RemotePullTimeDelayAlarmLogger remotePullTimeAlarmLogger = new RemotePullTimeDelayAlarmLogger();
        remotePullTimeAlarmLogger.setClientAlarmDataService(clientAlarmDataService);
        interceptors.add(remotePullTimeAlarmLogger);

        RemotePushTimeDelayAlarmLogger remotePushTimeAlarmLogger = new RemotePushTimeDelayAlarmLogger();
        remotePushTimeAlarmLogger.setClientAlarmDataService(clientAlarmDataService);
        interceptors.add(remotePushTimeAlarmLogger);

        chainedInterceptor = new ChainedInterceptor<BinlogHttpMessage>();
        chainedInterceptor.setInterceptors(interceptors);
        chainedInterceptor.start();
        interceptorHandler.setPumaInterceptor(chainedInterceptor);

        final BinlogRollbackHandler binlogRollbackHandler = new BinlogRollbackHandler();
        binlogRollbackHandler.setClientSessionService(clientSessionService);
        binlogRollbackHandler.setBinlogAckService(binlogAckService);

        final BinlogAckHandler binlogAckHandler = new BinlogAckHandler();
        binlogAckHandler.setBinlogAckService(binlogAckService);
        binlogAckHandler.setClientSessionService(clientSessionService);
        binlogAckHandler.setClientManager(clientManager);

        final BinlogGetHandler binlogGetHandler = new BinlogGetHandler();
        binlogGetHandler.setClientSessionService(clientSessionService);

        final BinlogSubscriptionHandler binlogSubscriptionHandler = new BinlogSubscriptionHandler();
        binlogSubscriptionHandler.setBinlogAckService(binlogAckService);
        binlogSubscriptionHandler.setClientSessionService(clientSessionService);
        binlogSubscriptionHandler.setClientManager(clientManager);
        binlogSubscriptionHandler.setClientAckService(clientAckService);

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
            clientManager.stop();
            chainedInterceptor.stop();
        }
    }
}
