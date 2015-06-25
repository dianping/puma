package com.dianping.puma.pumaserver.handler;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.codec.EventCodecFactory;
import com.dianping.puma.core.constant.SubscribeConstant;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.netty.entity.BinlogQuery;
import com.dianping.puma.core.util.ByteArrayUtils;
import com.dianping.puma.filter.EventFilterChain;
import com.dianping.puma.filter.EventFilterChainFactory;
import com.dianping.puma.pumaserver.AttributeKeys;
import com.dianping.puma.pumaserver.client.ClientInfo;
import com.dianping.puma.pumaserver.client.ClientType;
import com.dianping.puma.server.DefaultTaskExecutorContainer;
import com.dianping.puma.storage.BufferedEventChannel;
import com.dianping.puma.storage.EventChannel;
import com.dianping.puma.storage.EventStorage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;

public class BinlogQueryHandler extends SimpleChannelInboundHandler<BinlogQuery> {

    private ChannelHandlerContext ctx;
    private BinlogQuery binlogQuery;

    private EventCodec eventCodec;
    private EventFilterChain eventFilterChain;
    private EventChannel eventChannel;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, BinlogQuery binlogQuery) {
        ctx.channel().attr(AttributeKeys.CLIENT_INFO).set(new ClientInfo().setClientType(ClientType.PUMACLIENT));

        this.ctx = ctx;
        this.binlogQuery = binlogQuery;

        start();
        generateBinlogEvent();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        stop();
    }

    private void generateBinlogEvent() {
        try {
            while (true) {
                eventFilterChain.reset();
                ChangedEvent event = (ChangedEvent) eventChannel.next();
                if (event != null && eventFilterChain.doNext(event)) {
                    ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.buffer().writeBytes(codec(event));
                    ctx.channel().writeAndFlush(byteBuf).addListener(binlogEventGenerator);
                    break;
                }
            }

        } catch (IOException e) {
            exceptionCaught(ctx, e);
        }
    }

    private final ChannelFutureListener binlogEventGenerator = new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            if (future.isSuccess()) {
                generateBinlogEvent();
            } else {
                exceptionCaught(ctx, future.cause());
            }
        }
    };

    private byte[] codec(Event event) throws IOException {
        byte[] data = eventCodec.encode(event);
        byte[] dataLength = ByteArrayUtils.intToByteArray(data.length);
        byte[] buf = new byte[data.length + dataLength.length];
        System.arraycopy(dataLength, 0, buf, 0, dataLength.length);
        System.arraycopy(data, 0, buf, dataLength.length, data.length);
        return buf;
    }

    private void adjust() {
        BinlogInfo binlogInfo = binlogQuery.getBinlogInfo();
        if (binlogInfo == null || binlogInfo.getBinlogFile().equals("null")
                || binlogInfo.getBinlogFile().equals("mysql-bin.000000")) {
            binlogQuery.setSeq(SubscribeConstant.SEQ_FROM_LATEST);
        }
    }

    private void start() {
        try {
            eventCodec = EventCodecFactory.createCodec("json");
            eventFilterChain = EventFilterChainFactory.createEventFilterChain(
                    binlogQuery.isDdl(),
                    binlogQuery.isDml(),
                    binlogQuery.isTransaction(),
                    binlogQuery.getDatabaseTables());
            EventStorage eventStorage = DefaultTaskExecutorContainer.instance.getTaskStorage(binlogQuery.getTarget());

            adjust();
            eventChannel = new BufferedEventChannel(binlogQuery.getClientName(), eventStorage.getChannel(
                    binlogQuery.getSeq(),
                    binlogQuery.getServerId(),
                    binlogQuery.getBinlogInfo().getBinlogFile(),
                    binlogQuery.getBinlogInfo().getBinlogPosition(),
                    binlogQuery.getTimestamp()
            ), 5000);
            eventChannel.open();
        } catch (Exception e) {
            exceptionCaught(ctx, e);
        }
    }

    private void stop() {
        if (eventChannel != null) {
            eventChannel.close();
            eventChannel = null;
        }
        ctx.channel().close();
    }
}
