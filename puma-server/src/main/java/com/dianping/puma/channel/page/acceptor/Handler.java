package com.dianping.puma.channel.page.acceptor;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import com.dianping.puma.ComponentContainer;
import com.dianping.puma.channel.heartbeat.HeartbeatTask;
import com.dianping.puma.core.model.BinlogInfo;
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
import com.dianping.puma.storage.exception.StorageException;
import com.dianping.puma.utils.NetUtils;

public class Handler implements PageHandler<Context> {
	private static final Logger LOG = LoggerFactory.getLogger(Handler.class);

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
		String clientName = payload.getClientName() + new Date().toString();;
		String serverName = payload.getTarget();
		String clientIPAddr = NetUtils.getIpAddr(ctx.getHttpServletRequest());
		BinlogInfo binlogInfo = new BinlogInfo(payload.getBinlog(), payload.getBinlogPos());

		LOG.info("Client connecting: {}({}).", new Object[] { clientName, clientIPAddr });
		LOG.info("Client connecting info: server={}, binlogInfo={}.", new Object[] { serverName, binlogInfo });

		// Construct codec
		EventCodec codec;
		try {
			codec = EventCodecFactory.createCodec(payload.getCodecType());
		} catch (IllegalArgumentException e) {
			LOG.error("Client construct event codec error: {}.", e.getStackTrace());
			throw e;
		}

		// Construct server filter chain.
		EventFilterChain filterChain;
		try {
			filterChain = EventFilterChainFactory.createEventFilterChain(payload.isDdl(), payload.isDml(), payload
					.isNeedsTransactionMeta(), payload.getDatabaseTables());
		} catch (IllegalArgumentException e) {
			LOG.error("Client construct event filter chain error: {}.", e.getStackTrace());
			throw e;
		}
		// Get storage and channel.
		EventStorage storage = DefaultTaskExecutorContainer.instance.getTaskStorage(payload.getTarget());

		if (storage == null) {
			LOG.error("Client(" + clientName + ") cannot get storage-" + payload.getTarget() + ".");
			throw new IOException();
		}
		LOG.info("Client(" + clientName + ") get storage-" + payload.getTarget() + ".");
		res.setContentType("application/octet-stream");
		res.addHeader("Connection", "Keep-Alive");

		long seq = payload.getSeq();
		long serverId = payload.getServerId();
		String binlogFile = payload.getBinlog();
		long binlogPos = payload.getBinlogPos();
		long timeStamp = payload.getTimestamp();
		
		EventChannel channel;
		try {
			channel = new BufferedEventChannel(storage.getChannel(seq, serverId, binlogFile, binlogPos, timeStamp),
					5000);
		} catch (StorageException e1) {
			LOG.error("error occured " + e1.getMessage() + ", from " + NetUtils.getIpAddr(ctx.getHttpServletRequest()),
					e1);
			throw new IOException(e1);
		}
		
		// status report
		SystemStatusContainer.instance.addClientStatus(clientName, NetUtils.getIpAddr(ctx
				.getHttpServletRequest()), payload.getSeq(), payload.getTarget(), payload.isDml(), payload.isDdl(),
				payload.isNeedsTransactionMeta(), payload.getDatabaseTables(), payload.getCodecType());
		SystemStatusContainer.instance.updateClientBinlog(clientName, payload.getBinlog(), payload
				.getBinlogPos());
		
		ServerEventDelayMonitor serverEventDelayMonitor = ComponentContainer.SPRING.lookup("serverEventDelayMonitor");

		HeartbeatTask heartbeatTask = new HeartbeatTask(codec, res, clientName);

		while (true) {
			try {
				filterChain.reset();
				ChangedEvent event = (ChangedEvent)channel.next();

				if (event != null) {

					serverEventDelayMonitor.record(clientName, event.getExecuteTime());

					if (filterChain.doNext(event)) {
						byte[] data = codec.encode(event);
						res.getOutputStream().write(ByteArrayUtils.intToByteArray(data.length));
						res.getOutputStream().write(data);
						res.getOutputStream().flush();
						// status report
						SystemStatusContainer.instance.updateClientInfo(clientName, event.getSeq(),event.getBinlog(), event.getBinlogPos());
					}
				}
			} catch (Exception e) {
				Cat.logError("Puma.client.channelClosed:", e);
				SystemStatusContainer.instance.removeClient(clientName);
				heartbeatTask.cancelFuture();
				LOG.info("Client(" +clientName + ") failed. ", e);
				break;
			}
		}

		channel.close();
		SystemStatusContainer.instance.removeClient(clientName);
		heartbeatTask.cancelFuture();
		heartbeatTask = null;
	}
}
