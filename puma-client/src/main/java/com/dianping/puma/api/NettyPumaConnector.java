package com.dianping.puma.api;

import com.dianping.puma.core.netty.client.ClientConfig;
import com.dianping.puma.core.netty.client.TcpClient;
import com.dianping.puma.core.netty.entity.BinlogMessage;
import com.dianping.puma.core.netty.exception.PumaClientException;
import com.dianping.puma.core.netty.handler.ChannelHolderHandler;
import com.dianping.puma.core.netty.handler.HandlerFactory;
import com.dianping.puma.core.netty.remove.DefaultChannelHolder;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class NettyPumaConnector implements PumaConnector {

    private static final Logger logger = LoggerFactory.getLogger(NettyPumaConnector.class);

    private String clientName;

    private String remoteIp;
    private int remotePort;
    private int localPort;

    private TcpClient client;

    private final DefaultChannelHolder channelHolder = new DefaultChannelHolder();
    private final ChannelHolderHandler channelHolderHandler = new ChannelHolderHandler(channelHolder);

    public NettyPumaConnector(String clientName) {
        this.clientName = clientName;
    }

    @Override
    public synchronized void connect() throws PumaClientException {
        //todo:read from ???
        //this.remoteIp = remoteIp;
        //this.remotePort = remotePort;
        //this.localPort = localPort;

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
                handlers.put("HttpContentCompressor", new HttpContentCompressor());
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
    public BinlogMessage getWithoutAck(int batchSize) throws PumaClientException {
        return null;
    }

    @Override
    public BinlogMessage getWithoutAck(int batchSize, long timeout, TimeUnit timeUnit) throws PumaClientException {
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
    public void subscribe() throws PumaClientException {

    }
}
