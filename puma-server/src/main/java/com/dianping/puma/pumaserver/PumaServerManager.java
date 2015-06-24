package com.dianping.puma.pumaserver;

import com.dianping.puma.core.netty.handler.HandlerFactory;
import com.dianping.puma.core.netty.server.ServerConfig;
import com.dianping.puma.core.netty.server.TcpServer;
import com.dianping.puma.pumaserver.handler.HttpRouterHandler;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
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

    @PostConstruct
    public synchronized void init() {
        ServerConfig consoleConfig = new ServerConfig();
        consoleConfig.setPort(4040);

        consoleConfig.setHandlerFactory(new HandlerFactory() {
            @Override
            public Map<String, ChannelHandler> getHandlers() {
                Map<String, ChannelHandler> result = new LinkedHashMap<String, ChannelHandler>();
                result.put("HttpClientCodec", new HttpClientCodec());
                result.put("HttpContentDecompressor", new HttpContentDecompressor());
                result.put("HttpObjectAggregator", new HttpObjectAggregator(1024 * 1024 * 32));
                result.put("HttpRouterHandler", new HttpRouterHandler());
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
