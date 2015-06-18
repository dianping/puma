package com.dianping.puma.channel.page.acceptor;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import com.dianping.puma.ComponentContainer;
import com.dianping.puma.channel.heartbeat.HeartbeatManager;
import com.dianping.puma.core.constant.SubscribeConstant;
import com.dianping.puma.core.event.ServerErrorEvent;
import com.dianping.puma.monitor.ServerEventDelayMonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

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
		Lock sendLock = new ReentrantLock();

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
		if (binlogFile.equals("null") || binlogFile.equals("mysql-bin.000000")) {
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

		if (SystemStatusContainer.instance.getClientStatus(clientName) != null) {
			ServerErrorEvent event = new ServerErrorEvent("duplicated client error.");
			sendServerErrorEvent(res, sendLock, codec, event);

			return;
		}

		// Build event filter chain.
		EventFilterChain filterChain = EventFilterChainFactory
				.createEventFilterChain(ddl, dml, transaction, databaseTables);
		if (filterChain == null) {
			ServerErrorEvent event = new ServerErrorEvent("build event filter chain error.");
			sendServerErrorEvent(res, sendLock, codec, event);

			return;
		}

		// Find event storage.
		EventStorage storage = DefaultTaskExecutorContainer.instance.getTaskStorage(target);
		if (storage == null) {
			ServerErrorEvent event = new ServerErrorEvent("find event storage error.");
			sendServerErrorEvent(res, sendLock, codec, event);

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
			sendServerErrorEvent(res, sendLock, codec, event);

			return;
		}

		// Client connect success.
		SystemStatusContainer.instance.addClientStatus(clientName, NetUtils.getIpAddr(ctx.getHttpServletRequest()),
				seq, target, dml, ddl, transaction, databaseTables, codecType);
		SystemStatusContainer.instance.updateClientBinlog(clientName, binlogFile, binlogPosition, binlogIndex);

		ServerEventDelayMonitor serverEventDelayMonitor = ComponentContainer.SPRING.lookup("serverEventDelayMonitor");


		Lock stopLock = new ReentrantLock();
		HandlerContext context = new HandlerContext(clientName, res, stopLock, sendLock, codec, channel);
		HeartbeatManager heartbeatManager = new HeartbeatManager(codec, res, clientName, sendLock, serverEventDelayMonitor, context);

		while (!context.isStopped()) {

			try {
				filterChain.reset();
				ChangedEvent event = (ChangedEvent) channel.next();

				if (event != null && filterChain.doNext(event)) {
					sendChangedEvent(context, event);
					SystemStatusContainer.instance.updateClientInfo(clientName, event.getSeq(), event.getBinlogInfo());
				}

			} catch (Exception e) {
				break;
			}
		}

		context.quit();
	}

	private void sendChangedEvent(HandlerContext context, ChangedEvent event)
			throws IOException, InterruptedException, TimeoutException {
		byte[] data = context.getCodec().encode(event);

		try {
			if (context.getStopLock().tryLock(60, TimeUnit.SECONDS)) {
				context.getResponse().getOutputStream().write(ByteArrayUtils.intToByteArray(data.length));
				context.getResponse().getOutputStream().write(data);
				context.getResponse().getOutputStream().flush();
			} else {
				throw new TimeoutException("get lock timeout");
			}
		} finally {
			context.getStopLock().unlock();
		}
	}

	private void sendServerErrorEvent(HttpServletResponse response, Lock lock, EventCodec codec, ServerErrorEvent event)
			throws IOException {
		byte[] data = codec.encode(event);
		try {
			if (lock.tryLock(60, TimeUnit.SECONDS)) {
				try {
					response.getOutputStream().write(ByteArrayUtils.intToByteArray(data.length));
					response.getOutputStream().write(data);
					response.getOutputStream().flush();
				} finally {
					lock.unlock();
				}
			}
		} catch (InterruptedException e) {
			logger.warn("Puma server write serverErrorEvent interrupted.");
		}
	}

	public class HandlerContext {

		private boolean stopped = false;
		private String clientName;
		private HttpServletResponse response;
		private Lock stopLock;
		private Lock sendLock;
		private EventCodec codec;
		private EventChannel channel;

		public HandlerContext(String clientName, HttpServletResponse response, Lock stopLock, Lock sendLock, EventCodec codec, EventChannel channel) {
			this.clientName = clientName;
			this.response = response;
			this.stopLock = stopLock;
			this.sendLock = sendLock;
			this.codec = codec;
			this.channel = channel;
		}

		public void quit() {
			stopLock.lock();
			try {
				if (!stopped) {
					channel.close();
					channel = null;

					try {
						response.getOutputStream().close();
					} catch (IOException e) {
						// Ignore.
					}

					SystemStatusContainer.instance.removeClient(clientName);
				}
			} finally {
				stopLock.unlock();
			}
		}

		public void stop() {
			stopped = true;
		}

		public Lock getStopLock() {
			return stopLock;
		}

		public Lock getSendLock() {
			return sendLock;
		}

		public HttpServletResponse getResponse() {
			return response;
		}

		public EventCodec getCodec() {
			return codec;
		}

		public EventChannel getChannel() {
			return channel;
		}

		public boolean isStopped() {
			return stopped;
		}
	}
}
