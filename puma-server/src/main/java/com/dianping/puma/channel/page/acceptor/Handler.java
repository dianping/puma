package com.dianping.puma.channel.page.acceptor;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import com.dianping.puma.ComponentContainer;
import com.dianping.puma.channel.heartbeat.HeartbeatTask;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.container.client.ClientStateContainer;
import com.dianping.puma.core.model.state.client.ClientState;
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
import com.dianping.puma.core.event.Event;
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

	/*
	 * protected void endCatTransaction() { try { // complete current
	 * transaction immediately (for abnormal case only) MessageManager manager =
	 * Cat.getManager(); Transaction t1 = manager.getPeekTransaction();
	 * 
	 * t1.setStatus(Message.SUCCESS); t1.complete();
	 * 
	 * Transaction t2 = manager.getPeekTransaction();
	 * t2.setStatus(Message.SUCCESS); t2.complete(); } catch (Exception e) {
	 * LOG.error("Cat failed."); } }
	 */

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
		String clientName = payload.getClientName();
		String serverName = payload.getTarget();
		boolean dml = payload.isDml();
		boolean ddl = payload.isDdl();
		boolean transaction = payload.isNeedsTransactionMeta();
		String[] databaseTables = payload.getDatabaseTables();
		String clientIPAddr = NetUtils.getIpAddr(ctx.getHttpServletRequest());
		BinlogInfo binlogInfo = new BinlogInfo(payload.getBinlog(), payload.getBinlogPos());

		// Client connecting.
		Cat.logEvent("Client.Connecting", clientName + "(" + clientIPAddr + ")");
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

		ClientState clientState = new ClientState(payload.getClientName(), payload.getTarget(), NetUtils.getIpAddr(ctx
				.getHttpServletRequest()));
		ClientStateContainer clientStateContainer = ComponentContainer.SPRING.lookup("clientStateContainer");
		clientStateContainer.add(clientState);
		clientStateContainer.setSeq(payload.getClientName(), payload.getSeq());
		clientStateContainer.setBinlog(payload.getClientName(), new BinlogInfo(payload.getBinlog(), payload
				.getBinlogPos()));

		// status report
		SystemStatusContainer.instance.addClientStatus(payload.getClientName(), NetUtils.getIpAddr(ctx
				.getHttpServletRequest()), payload.getSeq(), payload.getTarget(), payload.isDml(), payload.isDdl(),
				payload.isNeedsTransactionMeta(), payload.getDatabaseTables(), payload.getCodecType());
		SystemStatusContainer.instance.updateClientBinlog(payload.getClientName(), payload.getBinlog(), payload
				.getBinlogPos());

		res.setContentType("application/octet-stream");
		res.addHeader("Connection", "Keep-Alive");

		long seq = payload.getSeq();
		long serverId = payload.getServerId();
		String binlogFile = payload.getBinlog();
		long binlogPos = payload.getBinlogPos();
		long timeStamp = payload.getTimestamp();
		LOG.info("Client(" + payload.getClientName() + ") get storage-" + payload.getTarget() + ".");
		if (storage == null) {
			clientStateContainer.remove(payload.getClientName());
			SystemStatusContainer.instance.removeClient(payload.getClientName());
			LOG.error("Client(" + payload.getClientName() + ") cannot get storage-" + payload.getTarget() + ".");
			throw new IOException();
		}

		EventChannel channel;
		try {
			channel = new BufferedEventChannel(storage.getChannel(seq, serverId, binlogFile, binlogPos, timeStamp),
					5000);
		} catch (StorageException e1) {
			LOG.error("error occured " + e1.getMessage() + ", from " + NetUtils.getIpAddr(ctx.getHttpServletRequest()),
					e1);
			throw new IOException(e1);
		}

		ServerEventDelayMonitor serverLaggingTimeMonitor = ComponentContainer.SPRING.lookup("serverLaggingTimeMonitor");

		HeartbeatTask heartbeatTask = new HeartbeatTask(codec, res);

		while (true) {
			try {
				filterChain.reset();

				Event event = channel.next();
				if (event != null && event instanceof ChangedEvent) {
					ChangedEvent changedEvent = (ChangedEvent) event;
					serverLaggingTimeMonitor.record(clientName, changedEvent.getExecuteTime());

					if (filterChain.doNext(changedEvent)) {
						byte[] data = codec.encode(changedEvent);
						synchronized(res){
							res.getOutputStream().write(ByteArrayUtils.intToByteArray(data.length));
							res.getOutputStream().write(data);
							res.getOutputStream().flush();
						}
						clientStateContainer.setSeq(payload.getClientName(), changedEvent.getSeq());
						clientStateContainer.setBinlog(payload.getClientName(), new BinlogInfo(
								changedEvent.getBinlog(), changedEvent.getBinlogPos()));

						// status report
						SystemStatusContainer.instance.updateClientSeq(payload.getClientName(), changedEvent.getSeq());
						// record success client seq
						SystemStatusContainer.instance.updateClientSuccessSeq(payload.getClientName(), changedEvent
								.getSeq());
						// update binlog
						SystemStatusContainer.instance.updateClientBinlog(payload.getClientName(), changedEvent
								.getBinlog(), changedEvent.getBinlogPos());

					}
				}
			} catch (Exception e) {
				Cat.getProducer().logError("puma.server.client.ChannelClosed.exception:", e);
				clientStateContainer.remove(payload.getClientName());
				SystemStatusContainer.instance.removeClient(payload.getClientName());
				heartbeatTask.shutdownExecutorService();
				LOG.info("Client(" + payload.getClientName() + ") failed. ", e);
				break;
			}
		}

		channel.close();
		clientStateContainer.remove(payload.getClientName());
		SystemStatusContainer.instance.removeClient(payload.getClientName());
		heartbeatTask.shutdownExecutorService();
		//long end = System.currentTimeMillis();
		//String ipAddress = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
		//Cat.getProducer().logEvent("ChannelClosed", ipAddress, Message.SUCCESS, "duration=" + (end - start));
	}
}
