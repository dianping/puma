package com.dianping.puma.pumaserver.handler;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.codec.EventCodecFactory;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.core.event.ServerErrorEvent;
import com.dianping.puma.core.netty.entity.BinlogQuery;
import com.dianping.puma.core.util.ByteArrayUtils;
import com.dianping.puma.filter.EventFilterChain;
import com.dianping.puma.filter.EventFilterChainFactory;
import com.dianping.puma.server.DefaultTaskExecutorContainer;
import com.dianping.puma.storage.BufferedEventChannel;
import com.dianping.puma.storage.EventChannel;
import com.dianping.puma.storage.EventStorage;
import com.dianping.puma.storage.exception.StorageException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.google.common.base.Preconditions.*;

public class BinlogQueryHandler extends SimpleChannelInboundHandler<BinlogQuery> {

	private static final Logger logger = LoggerFactory.getLogger(BinlogQueryHandler.class);

	private ChannelHandlerContext ctx;
	private BinlogQuery binlogQuery;

	private EventCodec eventCodec;
	private EventFilterChain eventFilterChain;
	private EventChannel eventChannel;

	@Override
	public void channelRead0(ChannelHandlerContext ctx, BinlogQuery binlogQuery) {
		this.ctx = ctx;
		this.binlogQuery = binlogQuery;

		try {
			startEventCodec();
			startEventFilterChain();
			startEventChannel();
		} catch (Exception e) {
			//writeServerErrorEvent();
		}

		writeBinlogEvent();
	}

	private void generateBinlogEvent() throws StorageException {
		ChangedEvent event = (ChangedEvent) eventChannel.next();

	}

	private byte[] codec(Event event) throws IOException {
		byte[] data = eventCodec.encode(event);
		
	}

	private void writeBinlogEvent() {
		eventFilterChain.reset();
		try {
			ChangedEvent event = (ChangedEvent) eventChannel.next();
			byte[] data = eventCodec.encode(event);

			ByteBuf byteBuf = ctx.alloc().directBuffer().writeBytes(ByteArrayUtils.intToByteArray(data.length)).writeBytes(data);
			ctx.channel().writeAndFlush(byteBuf).addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if (future.isSuccess()) {
						writeBinlogEvent();
					} else {
						future.cause().printStackTrace();

						stop();
					}
				}
			});
		} catch (Exception e) {
			stop();
		}
	}

	private void writeServerErrorEvent(ServerErrorEvent event) {
		byte[] data;
		try {
			data = eventCodec.encode(event);
		} catch (IOException e) {
			// Codec error, quit.
			stop();
			return;
		}

		ByteBuf byteBuf = ctx.alloc().directBuffer().writeBytes(ByteArrayUtils.intToByteArray(data.length)).writeBytes(data);
		ctx.channel().writeAndFlush(byteBuf).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (!future.isSuccess()) {
					// @TODO: logger error.
				}

				stop();
			}
		});
	}

	private void startEventCodec() {
		eventCodec = EventCodecFactory.createCodec("json");

		checkNotNull(eventCodec, "start binlog event codec error.");
	}

	private void startEventFilterChain() {
		eventFilterChain = EventFilterChainFactory.createEventFilterChain(
				binlogQuery.isDdl(),
				binlogQuery.isDml(),
				binlogQuery.isTransaction(),
				binlogQuery.getDatabaseTables());

		checkNotNull(eventFilterChain, "start binlog event filter chain error.");
	}

	private void startEventChannel() {
		EventStorage eventStorage = DefaultTaskExecutorContainer.instance.getTaskStorage(binlogQuery.getTarget());

		checkNotNull(eventStorage, "start binlog event channel error.");

		try {
			eventChannel = new BufferedEventChannel(binlogQuery.getClientName(), eventStorage.getChannel(
					binlogQuery.getSeq(),
					binlogQuery.getServerId(),
					binlogQuery.getBinlogInfo().getBinlogFile(),
					binlogQuery.getBinlogInfo().getBinlogPosition(),
					binlogQuery.getTimestamp()
			), 5000);
		} catch (Exception e) {
			throw new IllegalArgumentException("start binlog event channel error.");
		}
	}

	private void stopEventCodec() {
		eventCodec = null;
	}

	private void stopEventFilterChain() {
		eventFilterChain = null;
	}

	private void stopEventChannel() {
		eventChannel.close();
		eventChannel = null;
	}

	private void stop() {
		stopEventChannel();
		stopEventFilterChain();
		stopEventCodec();
	}
}
