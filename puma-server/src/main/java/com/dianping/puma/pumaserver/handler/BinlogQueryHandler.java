package com.dianping.puma.pumaserver.handler;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.netty.entity.BinlogMessage;
import com.dianping.puma.core.netty.entity.BinlogQuery;
import com.dianping.puma.pumaserver.ack.BinlogAckService;
import com.dianping.puma.pumaserver.channel.BinlogChannel;
import com.dianping.puma.pumaserver.channel.impl.ConstantBinlogChannel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;

public class BinlogQueryHandler extends SimpleChannelInboundHandler<BinlogQuery> {

	private boolean inited = false;

	private BinlogChannel binlogChannel = new ConstantBinlogChannel();

	private BinlogAckService binlogAckService;

	public BinlogQueryHandler(BinlogAckService binlogAckService) {
		this.binlogAckService = binlogAckService;
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, final BinlogQuery binlogQuery) throws IOException {
		if (!inited) {
			init(null);
			inited = true;
		}

		final BinlogMessage binlogMessage = new BinlogMessage();
		for (int i = 0; i != binlogQuery.getBatchSize(); ++i) {
			binlogMessage.addBinlogEvents(binlogChannel.next());
		}

		ctx.writeAndFlush(binlogMessage).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					ackIfNeeded(binlogQuery, binlogMessage);
				} else {
					// @TODO
				}
			}
		});
	}

	private void init(BinlogInfo binlogInfo) {
		binlogChannel.locate(binlogInfo);
	}

	private void ackIfNeeded(BinlogQuery binlogQuery, BinlogMessage binlogMessage) {
		if (binlogQuery.isAutoAck() && binlogMessage.size() > 0) {
			binlogAckService.save(null, binlogMessage.getLastBinlogInfo());
		}
	}
}
