package com.dianping.puma.pumaserver.handler.deprecated;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContentCompressor;

import java.io.IOException;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.codec.EventCodecFactory;
import com.dianping.puma.core.constant.SubscribeConstant;
import com.dianping.puma.core.dto.deprecated.DeprecatedBinlogQuery;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.util.ByteArrayUtils;
import com.dianping.puma.filter.EventFilterChain;
import com.dianping.puma.filter.EventFilterChainFactory;
import com.dianping.puma.pumaserver.handler.HttpResponseEncoder;
import com.dianping.puma.server.container.DefaultDeprecatedTaskContainer;
import com.dianping.puma.storage.EventChannel;
import com.dianping.puma.storage.EventStorage;
import com.dianping.puma.storage.channel.BufferedEventChannel;
import com.dianping.puma.storage.channel.DefaultEventChannel;

public class DeprecatedBinlogQueryHandler extends SimpleChannelInboundHandler<DeprecatedBinlogQuery> {

	private ChannelHandlerContext ctx;

	private DeprecatedBinlogQuery deprecatedBinlogQuery;

	private EventCodec eventCodec;

	private EventFilterChain eventFilterChain;

	private EventChannel eventChannel;

	@Override
	public void channelRead0(ChannelHandlerContext ctx, DeprecatedBinlogQuery deprecatedBinlogQuery) {
		ctx.channel().pipeline().remove(io.netty.handler.codec.http.HttpResponseEncoder.class);
		ctx.channel().pipeline().remove(HttpContentCompressor.class);
		ctx.channel().pipeline().remove(HttpResponseEncoder.class);

		this.ctx = ctx;
		this.deprecatedBinlogQuery = deprecatedBinlogQuery;

		start();
		generateBinlogEvent();
	}

	public void onException(ChannelHandlerContext ctx, Throwable cause) {
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
			onException(ctx, e);
		}
	}

	private final ChannelFutureListener binlogEventGenerator = new ChannelFutureListener() {
		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			if (future.isSuccess()) {
				generateBinlogEvent();
			} else {
				onException(ctx, future.cause());
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
		BinlogInfo binlogInfo = deprecatedBinlogQuery.getBinlogInfo();
		if (binlogInfo == null || binlogInfo.getBinlogFile().equals("null")
		      || binlogInfo.getBinlogFile().equals("mysql-bin.000000")) {
			deprecatedBinlogQuery.setSeq(SubscribeConstant.SEQ_FROM_LATEST);
		}
	}

	private void start() {
		try {
			eventCodec = EventCodecFactory.createCodec("json");
			eventFilterChain = EventFilterChainFactory.createEventFilterChain(deprecatedBinlogQuery.isDdl(),
			      deprecatedBinlogQuery.isDml(), deprecatedBinlogQuery.isTransaction(),
			      deprecatedBinlogQuery.getDatabaseTables());
			EventStorage eventStorage = DefaultDeprecatedTaskContainer.instance.getTaskStorage(deprecatedBinlogQuery.getTarget());

			adjust();

			DefaultEventChannel defaultChannel = new DefaultEventChannel(""); //TODO
//			defaultChannel.withDatabase(database);//TODO
			eventChannel = new BufferedEventChannel(deprecatedBinlogQuery.getClientName(), defaultChannel, 5000);

			if (deprecatedBinlogQuery.getSeq() == SubscribeConstant.SEQ_FROM_BINLOGINFO) {
				eventChannel.open(deprecatedBinlogQuery.getServerId(), deprecatedBinlogQuery.getBinlogInfo()
				      .getBinlogFile(), deprecatedBinlogQuery.getBinlogInfo().getBinlogPosition());
			} else if (deprecatedBinlogQuery.getSeq() == SubscribeConstant.SEQ_FROM_TIMESTAMP) {
				eventChannel.open(deprecatedBinlogQuery.getTimestamp());
			} else {
				eventChannel.open(deprecatedBinlogQuery.getSeq());
			}
		} catch (Exception e) {
			onException(ctx, e);
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
