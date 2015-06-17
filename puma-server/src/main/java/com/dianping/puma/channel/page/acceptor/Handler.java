package com.dianping.puma.channel.page.acceptor;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import com.dianping.puma.ComponentContainer;
import com.dianping.puma.channel.exception.ChannelClosedException;
import com.dianping.puma.channel.exception.PumaServerChannelException;
import com.dianping.puma.channel.heartbeat.HeartbeatTask;
import com.dianping.puma.config.PumaServerConfig;
import com.dianping.puma.core.constant.SubscribeConstant;
import com.dianping.puma.core.event.ServerErrorEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.filter.EventFilter;
import com.dianping.puma.monitor.ServerEventDelayMonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.puma.common.SystemStatusContainer;
import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.codec.EventCodecFactory;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.util.ByteArrayUtils;
import com.dianping.puma.filter.EventFilterChain;
import com.dianping.puma.filter.EventFilterChainFactory;
import com.dianping.puma.server.DefaultTaskExecutorContainer;
import com.dianping.puma.storage.BufferedEventChannel;
import com.dianping.puma.storage.EventChannel;
import com.dianping.puma.storage.EventStorage;
import com.dianping.puma.utils.NetUtils;

public class Handler implements PageHandler<Context> {
	private static final Logger logger = LoggerFactory.getLogger(Handler.class);

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "acceptor")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "acceptor")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Payload payload = ctx.getPayload();
		HttpServletResponse res = ctx.getHttpServletResponse();
		res.setContentType("application/octet-stream");
		res.addHeader("Connection", "Keep-Alive");
		Lock lock = new ReentrantLock();

		String clientName = payload.getClientName();
		String target = payload.getTarget();
		String clientIp = NetUtils.getIpAddr(ctx.getHttpServletRequest());

		boolean dml = payload.isDml();
		boolean ddl = payload.isDdl();
		boolean transaction = payload.isNeedsTransactionMeta();
		String[] databaseTables = payload.getDatabaseTables();

		// Codec("json").
		String codecType = payload.getCodecType();
		EventCodec codec = EventCodecFactory.createCodec(codecType);

		// sequence, binlog info, serverid.
		long seq = payload.getSeq();
		String binlogFile = payload.getBinlog();
		long binlogPosition = payload.getBinlogPos();
		int binlogIndex = payload.getEventIndex();
		long serverId = payload.getServerId();
		long timeStamp = payload.getTimestamp();
		if (binlogFile == null || binlogFile.equals("mysql-bin.000000")) {
			seq = SubscribeConstant.SEQ_FROM_LATEST;
		}

		String msg = (new StringBuilder())
				.append("client = ").append(clientName).append(", ")
				.append("client ip = ").append(clientIp).append(", ")
				.append("target = ").append(target).append(", ")
				.append("serverId = ").append(serverId).append(", ")
				.append("seq = ").append(seq).append(", ")
				.append("binlogFile = ").append(binlogFile).append(", ")
				.append("binlogPosition = ").append(binlogPosition).append(", ")
				.append("binlogIndex = ").append(binlogIndex)
				.toString();
		logger.info("Connection info: {}.", msg);

		// Build event filter chain.
		EventFilterChain filterChain = EventFilterChainFactory
				.createEventFilterChain(ddl, dml, transaction, databaseTables);
		if (filterChain == null) {
			ServerErrorEvent event = new ServerErrorEvent("build event filter chain error.");
			sendServerErrorEvent(res, lock, codec, event);

			return;
		}

		// Find event storage.
		EventStorage storage = DefaultTaskExecutorContainer.instance.getTaskStorage(target);
		if (storage == null) {
			ServerErrorEvent event = new ServerErrorEvent("find event storage error.");
			sendServerErrorEvent(res, lock, codec, event);

			return;
		}

		// Build event storage channel.
		EventChannel channel;
		try {
			channel = new BufferedEventChannel(clientName,
					storage.getChannel(seq, serverId, binlogFile, binlogPosition, timeStamp), 5000);
			channel.open();
		} catch (Exception e) {
			ServerErrorEvent event = new ServerErrorEvent("build event storage channel error.");
			sendServerErrorEvent(res, lock, codec, event);

			return;
		}

		// Client connect success.
		SystemStatusContainer.instance.addClientStatus(clientName, NetUtils.getIpAddr(ctx.getHttpServletRequest()),
				seq, target, dml, ddl, transaction, databaseTables, codecType);
		SystemStatusContainer.instance.updateClientBinlog(clientName, binlogFile, binlogPosition, binlogIndex);

		ServerEventDelayMonitor serverEventDelayMonitor = ComponentContainer.SPRING.lookup("serverEventDelayMonitor");

		// Start heartbeat.
		HeartbeatTask heartbeatTask = new HeartbeatTask(codec, res, clientName, lock, serverEventDelayMonitor);

		while (true) {
			try {
				filterChain.reset();
				ChangedEvent event = (ChangedEvent) channel.next();

				if (event != null) {

					serverEventDelayMonitor.record(clientName, event.getExecuteTime());

					if (filterChain.doNext(event)) {
						byte[] data = codec.encode(event);
						if (lock.tryLock(60, TimeUnit.SECONDS)) {
							try {
								res.getOutputStream().write(ByteArrayUtils.intToByteArray(data.length));
								res.getOutputStream().write(data);
								res.getOutputStream().flush();
							} finally {
								lock.unlock();
							}
						} else {
							throw new IOException("Client obtain write changedEvent lock failed.");
						}
						// status report
						SystemStatusContainer.instance.updateClientInfo(clientName, event.getSeq(), event.getBinlogInfo());
					}
				}

			} catch (InterruptedException e) {
				logger.warn("ClientName: " + clientName + ", Puma server write changedEvent interrupted.");
			} catch (Exception e) {
				Cat.logError("Puma.client.channelClosed:", new ChannelClosedException("ClientName: " + clientName
						+ ", ClientIp: " + clientIp, e));
				SystemStatusContainer.instance.removeClient(clientName);
				serverEventDelayMonitor.remove(clientName);
				heartbeatTask.cancelFuture();
				logger.info("Client(" + clientName + ") failed. ", e);
				break;
			}
		}

		channel.close();
		SystemStatusContainer.instance.removeClient(clientName);
		heartbeatTask.cancelFuture();
		heartbeatTask = null;
	}

	private void sendServerErrorEvent(HttpServletResponse response, Lock lock, EventCodec codec, ServerErrorEvent event)
			throws IOException {
		byte[] data = codec.encode(event);
		if (lock.tryLock()) {
			try {
				response.getOutputStream().write(ByteArrayUtils.intToByteArray(data.length));
				response.getOutputStream().write(data);
				response.getOutputStream().flush();
			} finally {
				lock.unlock();
			}
		}
	}
}
