package com.dianping.puma.pumaserver.handler;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.netty.entity.BinlogAck;
import com.dianping.puma.core.netty.entity.BinlogMessage;
import com.dianping.puma.core.netty.entity.BinlogQuery;
import com.dianping.puma.pumaserver.channel.BinlogChannel;
import com.dianping.puma.pumaserver.client.ClientSession;
import com.dianping.puma.pumaserver.service.BinlogAckService;
import com.dianping.puma.pumaserver.service.ClientSessionService;
import com.google.common.base.Stopwatch;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class BinlogQueryHandler extends SimpleChannelInboundHandler<BinlogQuery> {

    private final BinlogAckService binlogAckService;

    private final ClientSessionService clientSessionService;

    public BinlogQueryHandler(BinlogAckService binlogAckService, ClientSessionService clientSessionService) {
        this.binlogAckService = binlogAckService;
        this.clientSessionService = clientSessionService;
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, final BinlogQuery binlogQuery) throws IOException {
        ClientSession clientSession = clientSessionService.get(binlogQuery.getClientName(), binlogQuery.getToken());
        if (clientSession == null) {
            throw new RuntimeException("must subscribe before query binlog.");
        }

        final String clientName = clientSession.getClientName();
        if (clientName == null) {
            throw new NullPointerException("null client name.");
        }

        final BinlogChannel binlogChannel = clientSession.getBinlogChannel();
        if (binlogChannel == null) {
            throw new NullPointerException("null binlog channel.");
        }

        final BinlogMessage binlogMessage = (binlogQuery.getTimeout() <= 0) ?
                fillBinlogMessage(binlogChannel, binlogQuery.getBatchSize()) :
                fillBinlogMessageWithTimeout(binlogChannel, binlogQuery.getBatchSize(), binlogQuery.getTimeout(), binlogQuery.getTimeUnit());

        ctx.writeAndFlush(binlogMessage).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {

                    if (binlogQuery.isAutoAck() && binlogMessage.size() > 0) {
                        BinlogAck binlogAck = new BinlogAck();
                        binlogAck.setBinlogInfo(binlogMessage.getLastBinlogInfo());
                        binlogAckService.save(clientName, binlogAck);
                    }

                } else {
                    // @todo
                }
            }
        });
    }

    private BinlogMessage fillBinlogMessage(final BinlogChannel binlogChannel, int batchSize) {
        BinlogMessage binlogMessage = new BinlogMessage();
        for (int i = 0; i != batchSize; ++i) {
            binlogMessage.addBinlogEvents(binlogChannel.next());
        }
        return binlogMessage;
    }

    private BinlogMessage fillBinlogMessageWithTimeout(final BinlogChannel binlogChannel, int batchSize, long timeout,
                                                       TimeUnit timeUnit) {
        BinlogMessage binlogMessage = new BinlogMessage();
        long nextTimeout = timeout;
        Stopwatch stopwatch = Stopwatch.createUnstarted();

        for (int i = 0; i != batchSize; ++i) {
            if (nextTimeout <= 0) {
                break;
            }

            stopwatch.reset();
            stopwatch.start();

            ChangedEvent binlogEvent = binlogChannel.next(nextTimeout, timeUnit);
            stopwatch.stop();

            if (binlogEvent == null) {
                break;
            } else {
                binlogMessage.addBinlogEvents(binlogEvent);
                nextTimeout = nextTimeout - stopwatch.elapsed(timeUnit);
            }
        }

        return binlogMessage;
    }
}
