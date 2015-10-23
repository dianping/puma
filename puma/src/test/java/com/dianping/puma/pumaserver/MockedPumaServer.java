package com.dianping.puma.pumaserver;

import com.dianping.puma.core.dto.BinlogMessage;
import com.dianping.puma.core.dto.binlog.request.BinlogAckRequest;
import com.dianping.puma.core.dto.binlog.request.BinlogGetRequest;
import com.dianping.puma.core.dto.binlog.request.BinlogSubscriptionRequest;
import com.dianping.puma.core.dto.binlog.response.BinlogAckResponse;
import com.dianping.puma.core.dto.binlog.response.BinlogGetResponse;
import com.dianping.puma.core.dto.binlog.response.BinlogSubscriptionResponse;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.util.sql.DMLType;
import com.dianping.puma.pumaserver.client.PumaClientsHolder;
import com.dianping.puma.pumaserver.handler.*;
import com.dianping.puma.pumaserver.server.ServerConfig;
import com.dianping.puma.pumaserver.server.TcpServer;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Dozer @ 8/10/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class MockedPumaServer {
    protected static final ChannelHolderHandler channelHolderHandler = new ChannelHolderHandler(new PumaClientsHolder());

    public static void main(String[] args) {
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
                result.put("BinlogSubscriptionHandler", new BinlogSubscriptionHandler());
                result.put("BinlogQueryHandler", new BinlogGetHandler());
                result.put("BinlogAckHandler", new BinlogAckHandler());
                result.put("ExceptionHandler", ExceptionHandler.INSTANCE);
                return result;
            }
        });
        TcpServer server = new TcpServer(consoleConfig);
        server.init();
    }

    static class BinlogSubscriptionHandler extends SimpleChannelInboundHandler<BinlogSubscriptionRequest> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, BinlogSubscriptionRequest msg) throws Exception {
            BinlogSubscriptionResponse binlogSubscriptionResponse = new BinlogSubscriptionResponse();
            binlogSubscriptionResponse.setToken("token");
            ctx.channel().writeAndFlush(binlogSubscriptionResponse);
        }
    }

    static class BinlogAckHandler extends SimpleChannelInboundHandler<BinlogAckRequest> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, BinlogAckRequest msg) throws Exception {
            ctx.channel().writeAndFlush(new BinlogAckResponse());
        }
    }

    static class BinlogGetHandler extends SimpleChannelInboundHandler<BinlogGetRequest> {
        private AtomicLong seq = new AtomicLong();

        private RowChangedEvent getRowChangedEvent() {
            long id = seq.incrementAndGet();
            RowChangedEvent event = new RowChangedEvent();
            event.setBinlogInfo(new BinlogInfo(1, "file", id, 0, id));
            event.setDmlType(DMLType.INSERT);
            event.setTable("debug");

            Map<String, RowChangedEvent.ColumnInfo> columns = new HashMap<String, RowChangedEvent.ColumnInfo>();
            columns.put("id", new RowChangedEvent.ColumnInfo(true, null, id));
            columns.put("name", new RowChangedEvent.ColumnInfo(false, null, String.valueOf(id)));
            event.setColumns(columns);
            return event;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, BinlogGetRequest msg) throws Exception {
            msg.setCodec("raw");
            BinlogGetResponse response = new BinlogGetResponse();
            BinlogMessage message = new BinlogMessage();
            for (int k = 0; k < msg.getBatchSize(); k++) {
                message.addBinlogEvents(getRowChangedEvent());
            }
            response.setBinlogMessage(message);
            response.setBinlogGetRequest(msg);
            ctx.channel().writeAndFlush(response);
        }
    }
}
