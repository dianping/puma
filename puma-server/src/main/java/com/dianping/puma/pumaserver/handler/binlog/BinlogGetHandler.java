package com.dianping.puma.pumaserver.handler.binlog;

import com.dianping.puma.core.dto.BinlogAck;
import com.dianping.puma.core.dto.BinlogMessage;
import com.dianping.puma.core.dto.binlog.request.BinlogGetRequest;
import com.dianping.puma.core.dto.binlog.response.BinlogGetResponse;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.pumaserver.channel.BinlogChannel;
import com.dianping.puma.pumaserver.client.ClientSession;
import com.dianping.puma.pumaserver.service.BinlogAckService;
import com.dianping.puma.pumaserver.service.ClientSessionService;
import com.google.common.base.Stopwatch;
import io.netty.channel.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@ChannelHandler.Sharable
public class BinlogGetHandler extends SimpleChannelInboundHandler<BinlogGetRequest> {

    private BinlogAckService binlogAckService;
    private ClientSessionService clientSessionService;

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, final BinlogGetRequest binlogGetRequest) throws IOException {
        final ClientSession session = clientSessionService.get(binlogGetRequest.getClientName(), binlogGetRequest.getToken());

        final BinlogMessage binlogMessage = (binlogGetRequest.getTimeout() <= 0)
                ?
                fillBinlogMessage(
                        session.getBinlogChannel(),
                        binlogGetRequest.getBatchSize())
                :
                fillBinlogMessageWithTimeout(
                        session.getBinlogChannel(),
                        binlogGetRequest.getBatchSize(),
                        binlogGetRequest.getTimeout(),
                        binlogGetRequest.getTimeUnit()
                );

        BinlogGetResponse binlogGetResponse = new BinlogGetResponse();
        binlogGetResponse.setBinlogMessage(binlogMessage);
        ctx.writeAndFlush(binlogGetResponse).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {

                    if (binlogGetRequest.isAutoAck() && binlogMessage.size() > 0) {
                        BinlogAck binlogAck = new BinlogAck();
                        binlogAck.setBinlogInfo(binlogMessage.getLastBinlogInfo());
                        binlogAckService.save(session.getClientName(), binlogAck);
                    }

                } else {
                    // @todo
                }
            }
        });

		/*
        ctx.writeAndFlush(binlogMessage).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {

					if (binlogQuery.isAutoAck() && binlogMessage.size() > 0) {
						BinlogAck binlogAck = new BinlogAck();
						binlogAck.setBinlogInfo(binlogMessage.getLastBinlogInfo());
						binlogAckService.save(session.getClientName(), binlogAck);
					}

				} else {
					// @todo
				}
			}
		});*/
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

    public void setBinlogAckService(BinlogAckService binlogAckService) {
        this.binlogAckService = binlogAckService;
    }

    public void setClientSessionService(ClientSessionService clientSessionService) {
        this.clientSessionService = clientSessionService;
    }
}
