package com.dianping.puma.api;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.netty.client.ClientConfig;
import com.dianping.puma.core.netty.client.TcpClient;
import com.dianping.puma.core.netty.entity.BinlogMessage;
import com.dianping.puma.core.netty.exception.PumaClientException;
import com.dianping.puma.core.netty.handler.ChannelHolderHandler;
import com.dianping.puma.core.netty.handler.HandlerFactory;
import com.dianping.puma.core.netty.remove.DefaultChannelHolder;
import com.dianping.puma.core.util.ConvertHelper;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.*;
import org.apache.http.annotation.NotThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@NotThreadSafe
public class SinglePumaConnector implements PumaConnector {

    private static final Logger logger = LoggerFactory.getLogger(SinglePumaConnector.class);

    private final String clientName;
    private final String remoteIp;
    private final int remotePort;
    private final int localPort;

    private TcpClient client;

    private final BlockingQueue<BinlogMessage> queue = new LinkedBlockingQueue<BinlogMessage>(1);
    private final BinlogMessageDecoder binlogMessageDecoder = new BinlogMessageDecoder();
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
    public void connect() throws PumaClientException {
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
                handlers.put("binlogMessageDecoder", binlogMessageDecoder);
                return handlers;
            }
        });

        client = new TcpClient(config);
        client.init();
    }

    @Override
    public void disconnect() throws PumaClientException {
        if (client != null) {
            client.close();
            client = null;
        }
    }

    @Override
    public BinlogMessage get(int batchSize) throws PumaClientException, InterruptedException {
        return get(batchSize, 0, null);
    }

    @Override
    public BinlogMessage get(int batchSize, long timeout, TimeUnit timeUnit) throws PumaClientException, InterruptedException {
        queue.clear();
        QueryStringEncoder queryStringEncoder = new QueryStringEncoder("/binlog/get");
        queryStringEncoder.addParam("batchSize", String.valueOf(batchSize));
        queryStringEncoder.addParam("timeout", String.valueOf(timeout));
        queryStringEncoder.addParam("timeUnit", timeUnit.toString());
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, queryStringEncoder.toString());
        channelHolder.writeAndFlush(request).sync();

        try {
            return queue.poll(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new PumaClientException(true, false, "Get message timeout!");
        }
    }

    @Override
    public BinlogMessage getWithAck(int batchSize) throws PumaClientException, InterruptedException {
        BinlogMessage message = get(batchSize);
        ack(message.getLastBinlogInfo());
        return message;
    }

    @Override
    public BinlogMessage getWithAck(int batchSize, long timeout, TimeUnit timeUnit) throws PumaClientException, InterruptedException {
        BinlogMessage message = get(batchSize, timeout, timeUnit);
        ack(message.getLastBinlogInfo());
        return message;
    }

    @Override
    public void ack(BinlogInfo binlogInfo) throws PumaClientException, InterruptedException {
        QueryStringEncoder queryStringEncoder = new QueryStringEncoder("/binlog/ack");
//        queryStringEncoder.addParam("batchSize", String.valueOf(batchSize));
//        queryStringEncoder.addParam("timeout", String.valueOf(timeout));
//        queryStringEncoder.addParam("timeUnit", timeUnit.toString());

        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, queryStringEncoder.toString());
        channelHolder.writeAndFlush(request).sync();
    }

    @Override
    public void rollback(BinlogInfo binlogInfo) throws PumaClientException {
        throw new NotImplementedException();
    }

    @Override
    public void rollback() throws PumaClientException {
        throw new NotImplementedException();
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


    @ChannelHandler.Sharable
    public class BinlogMessageDecoder extends MessageToMessageDecoder<HttpObject> {
        @Override
        protected void decode(ChannelHandlerContext ctx, HttpObject msg, List<Object> out) throws Exception {
            if (msg instanceof FullHttpResponse) {
                FullHttpResponse response = (FullHttpResponse) msg;
                byte[] data = null;

                if (response.content().hasArray()) {
                    data = response.content().array();
                } else {
                    data = new byte[response.content().readableBytes()];
                    response.content().readBytes(data);
                }

                BinlogMessage message = ConvertHelper.fromBytes(data, 0, data.length, BinlogMessage.class);
                queue.offer(message);
            }
        }
    }
}
