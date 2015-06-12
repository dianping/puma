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

		String clientName = payload.getClientName() + "-" + Long.toString(System.currentTimeMillis());
		String serverName = payload.getTarget();
		String clientIp = NetUtils.getIpAddr(ctx.getHttpServletRequest());
		BinlogInfo binlogInfo = new BinlogInfo(payload.getBinlog(), payload.getBinlogPos());
		boolean dml = payload.isDml();
		boolean ddl = payload.isDdl();
		boolean transaction = payload.isNeedsTransactionMeta();
		String[] databaseTables = payload.getDatabaseTables();

		HttpServletResponse res = ctx.getHttpServletResponse();

		logger.info("Client connecting: {}({}).", new Object[] { clientName, clientIp });
		logger.info("Client connecting info: server={}, binlogInfo={}, seq={}.",
				new Object[] { serverName, binlogInfo, payload.getSeq() });

		// Construct codec
		EventCodec codec;
		try {
			codec = EventCodecFactory.createCodec(payload.getCodecType());
		} catch (IllegalArgumentException e) {
			logger.error("Client construct event codec error: {}.", e.getStackTrace());
			throw e;
		}

		res.setContentType("application/octet-stream");
		res.addHeader("Connection", "Keep-Alive");

		long seq = payload.getSeq();
		long serverId = payload.getServerId();
		String binlogFile = payload.getBinlog();
		long binlogPos = payload.getBinlogPos();
		long timeStamp = payload.getTimestamp();
		String target = payload.getTarget();

		// status report
		SystemStatusContainer.instance.addClientStatus(clientName, NetUtils.getIpAddr(ctx.getHttpServletRequest()),
				payload.getSeq(), payload.getTarget(), payload.isDml(), payload.isDdl(), payload
						.isNeedsTransactionMeta(), payload.getDatabaseTables(), payload.getCodecType());
		SystemStatusContainer.instance.updateClientBinlog(clientName, payload.getBinlog(), payload.getBinlogPos());

		ServerEventDelayMonitor serverEventDelayMonitor = ComponentContainer.SPRING.lookup("serverEventDelayMonitor");
		Lock lock = new ReentrantLock();
		HeartbeatTask heartbeatTask = new HeartbeatTask(codec, res, clientName, lock, serverEventDelayMonitor);

		// Build event filter chain.
		EventFilterChain filterChain;
		try {
			filterChain = EventFilterChainFactory.createEventFilterChain(ddl, dml, transaction, databaseTables);
		} catch (Exception e) {
			String msg = String.format("Puma server(%s) building event filter chain for client(%s, %s) error.",
					serverName, clientName, clientIp);


			logger.error("Client construct event filter chain error: {}.", e.getStackTrace());
			throw e;
		}

		// Find storage.
		EventStorage storage = DefaultTaskExecutorContainer.instance.getTaskStorage(payload.getTarget());
		if (storage == null) {
			String msg = String
					.format("Puma server(%s) finding storage for client(%s, %s) error.", serverName, clientName, clientIp);
			PumaServerChannelException pe = new PumaServerChannelException(msg);

			logger.error(msg, pe);
			Cat.logError(msg, pe);

			throw pe;
		}

		// Build channel from storage and open.
		EventChannel channel;
		try {
			channel = new BufferedEventChannel(clientName,
					storage.getChannel(seq, serverId, binlogFile, binlogPos, timeStamp), 5000);
			channel.open();
		} catch (Exception e) {
			String msg = String
					.format("Puma server(%s) building channel for client(%s, %s).", serverName, clientName, clientIp);
			PumaServerChannelException pe = new PumaServerChannelException(msg, e);

			logger.error(msg, pe);
			Cat.logError(msg, pe);

			throw pe;
		}

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
						SystemStatusContainer.instance.updateClientInfo(clientName, event.getSeq(), event.getBinlog(),
								event.getBinlogPos());
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
}
