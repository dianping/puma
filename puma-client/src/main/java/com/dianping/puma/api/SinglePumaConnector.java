package com.dianping.puma.api;

import com.dianping.puma.core.netty.client.ClientConfig;
import com.dianping.puma.core.netty.client.TcpClient;
import com.dianping.puma.core.netty.entity.BinlogMessage;
import com.dianping.puma.core.netty.exception.PumaClientException;
import com.dianping.puma.core.netty.handler.ChannelHolderHandler;
import com.dianping.puma.core.netty.handler.HandlerFactory;
import com.dianping.puma.core.netty.remove.DefaultChannelHolder;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SinglePumaConnector implements PumaConnector {

    private static final Logger logger = LoggerFactory.getLogger(SinglePumaConnector.class);

    private final String clientName;
    private final String remoteIp;
    private final int remotePort;
    private final int localPort;

    private TcpClient client;

    private final DefaultChannelHolder channelHolder = new DefaultChannelHolder();
    private final ChannelHolderHandler channelHolderHandler = new ChannelHolderHandler(channelHolder) {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            if (subscriptionRequest != null) {
                ctx.channel().writeAndFlush(subscriptionRequest.copy());
            }
        }
    };

    private volatile DefaultFullHttpRequest subscriptionRequest = null;

    public SinglePumaConnector(String clientName, String remoteIp, int remotePort) {
        this.clientName = clientName;
        this.remoteIp = remoteIp;
        this.remotePort = remotePort;
        this.localPort = 0;
    }

    public SinglePumaConnector(String clientName, String remoteIp, int remotePort, int localPort) {
        this.clientName = clientName;
        this.remoteIp = remoteIp;
        this.remotePort = remotePort;
        this.localPort = localPort;
    }

    @Override
    public synchronized void connect() throws PumaClientException {
        doConnect();
    }

    private void doConnect() throws PumaClientException {
        ClientConfig config = new ClientConfig();
        config.setRemoteIp(remoteIp);
        config.setRemotePort(remotePort);
        config.setLocalPort(localPort);
        config.setHandlerFactory(new HandlerFactory() {
            @Override
            public Map<String, ChannelHandler> getHandlers() {
                Map<String, ChannelHandler> handlers = new LinkedHashMap<String, ChannelHandler>();
                handlers.put("channelHolderHandler", channelHolderHandler);
                handlers.put("HttpClientCodec", new HttpClientCodec());
                handlers.put("HttpContentDecompressor", new HttpContentDecompressor());
                handlers.put("HttpObjectAggregator", new HttpObjectAggregator(1024 * 1024 * 32));
                return handlers;
            }
        });

        client = new TcpClient(config);
        client.init();
    }

    @Override
    public synchronized void disconnect() throws PumaClientException {
        if (client != null) {
            client.close();
            client = null;
        }
    }

    @Override
    public BinlogMessage get(int batchSize) throws PumaClientException {
        return null;
    }

    @Override
    public BinlogMessage get(int batchSize, long timeout, TimeUnit timeUnit) throws PumaClientException {
        return null;
    }

    @Override
    public BinlogMessage getWithAck(int batchSize) throws PumaClientException {
        return null;
    }

    @Override
    public BinlogMessage getWithAck(int batchSize, long timeout, TimeUnit timeUnit) throws PumaClientException {
        return null;
    }

    @Override
    public void ack(long batchId) throws PumaClientException {

    }

    @Override
    public void rollback(long batchId) throws PumaClientException {

    }

    @Override
    public void rollback() throws PumaClientException {

    }

    @Override
    public void subscribe(boolean dml, boolean ddl, boolean transaction, String database, String... tables) throws PumaClientException {
        QueryStringEncoder queryStringEncoder = new QueryStringEncoder("/binlog/subscribe");
        queryStringEncoder.addParam("clientName", clientName);
        queryStringEncoder.addParam("database", database);
        queryStringEncoder.addParam("dml", String.valueOf(dml));
        queryStringEncoder.addParam("ddl", String.valueOf(ddl));
        queryStringEncoder.addParam("transaction", String.valueOf(transaction));
        for (String table : tables) {
            queryStringEncoder.addParam("table", table);
        }

        subscriptionRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, queryStringEncoder.toString());
        channelHolder.writeAndFlush(subscriptionRequest.copy());
    }
}
