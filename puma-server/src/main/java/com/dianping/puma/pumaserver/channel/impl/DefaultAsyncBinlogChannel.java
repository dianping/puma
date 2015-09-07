package com.dianping.puma.pumaserver.channel.impl;

import com.dianping.cat.Cat;
import com.dianping.puma.core.constant.SubscribeConstant;
import com.dianping.puma.core.dto.BinlogMessage;
import com.dianping.puma.core.dto.binlog.request.BinlogGetRequest;
import com.dianping.puma.core.dto.binlog.response.BinlogGetResponse;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.core.event.ServerErrorEvent;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.eventbus.DefaultEventBus;
import com.dianping.puma.eventbus.event.ClientPositionChangedEvent;
import com.dianping.puma.pumaserver.channel.AsyncBinlogChannel;
import com.dianping.puma.pumaserver.exception.binlog.BinlogChannelException;
import com.dianping.puma.status.SystemStatusManager;
import com.dianping.puma.storage.EventChannel;
import com.dianping.puma.storage.channel.DefaultEventChannel;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class DefaultAsyncBinlogChannel implements AsyncBinlogChannel {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultAsyncBinlogChannel.class);

	protected static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

	private final String clientName;

	private volatile String database;

	private volatile boolean stopped = false;

	private volatile EventChannel eventChannel;

	private final BlockingQueue<BinlogGetRequest> requests = new LinkedBlockingQueue<BinlogGetRequest>(5);

	public DefaultAsyncBinlogChannel(String clientName) {
		this.clientName = clientName;
	}

	@Override
	public void init(
		long sc,
		BinlogInfo binlogInfo,
		String database,
		List<String> tables,
		boolean dml,
		boolean ddl,
		boolean transaction
	) throws BinlogChannelException {
		try {
			this.database = database;
			this.eventChannel = initChannel(sc, binlogInfo, tables, dml, ddl, transaction);
			THREAD_POOL.execute(new AsyncTask(new WeakReference<DefaultAsyncBinlogChannel>(this)));
			DefaultEventBus.INSTANCE.register(this);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new BinlogChannelException("find event storage failure", e);
		}
	}

	@Subscribe
	public void listenClientPositionChanged(ClientPositionChangedEvent event) {
		if (event.getClientName() == null || !event.getClientName().equals(clientName)) {
			return;
		}

		if (this.eventChannel == null) {
			return;
		}

		try {
			EventChannel newEventChannel;
			try {
				newEventChannel = initChannel(
					SubscribeConstant.SEQ_FROM_BINLOGINFO,
					event.getBinlogInfo(), Lists.newArrayList(this.eventChannel.getTables()),
					this.eventChannel.getDml(), this.eventChannel.getDdl(), this.eventChannel.getTransaction());
			} catch (IOException e) {
				newEventChannel = initChannel(
					SubscribeConstant.SEQ_FROM_TIMESTAMP,
					event.getBinlogInfo(), Lists.newArrayList(this.eventChannel.getTables()),
					this.eventChannel.getDml(), this.eventChannel.getDdl(), this.eventChannel.getTransaction());
			}

			EventChannel oldEventChannel = this.eventChannel;
			this.eventChannel = newEventChannel;

			oldEventChannel.close();
			Cat.logEvent("Switch.ClientPosition", String.format("%s %s", clientName, event.getBinlogInfo().toString()));
		} catch (IOException e) {
			Cat.logError(
				String.format("Switch ClientPosition Failed! %s %s", clientName, event.getBinlogInfo().toString()), e);
		}
	}

	protected EventChannel initChannel(long sc, BinlogInfo binlogInfo, List<String> tables,
		boolean dml, boolean ddl, boolean transaction) throws IOException {
		DefaultEventChannel newEventChannel = new DefaultEventChannel(database);
		newEventChannel.withTables(tables.toArray(new String[tables.size()]));
		newEventChannel.withDml(dml);
		newEventChannel.withDdl(ddl);
		newEventChannel.withTransaction(transaction);

		if (sc == SubscribeConstant.SEQ_FROM_BINLOGINFO) {
			newEventChannel.open(binlogInfo.getServerId(), binlogInfo.getBinlogFile(), binlogInfo.getBinlogPosition());
		} else if (sc == SubscribeConstant.SEQ_FROM_TIMESTAMP) {
			newEventChannel.open(binlogInfo.getTimestamp());
		} else {
			newEventChannel.open(sc);
		}
		return newEventChannel;
	}

	@Override
	public void destroy() {
		try {
			DefaultEventBus.INSTANCE.unregister(this);
		} catch (Exception ignore) {

		}
		stopped = true;
		eventChannel.close();
	}

	@Override
	public boolean addRequest(BinlogGetRequest request) {
		return requests.offer(request);
	}

	static class AsyncTask implements Runnable {
		private static final Logger LOG = LoggerFactory.getLogger(AsyncTask.class);

		private static final int CACHE_SIZE = 1000;

		private static final int EMPTY_SLEEP_TIME = 5;

		private final WeakReference<DefaultAsyncBinlogChannel> parent;

		public AsyncTask(WeakReference<DefaultAsyncBinlogChannel> parent) {
			this.parent = parent;
		}

		private boolean threadNameHasSet = false;

		@Override
		public void run() {
			List<Event> results = new ArrayList<Event>();

			try {
				BinlogGetRequest req = null;

				while (!getParent().stopped && !Thread.currentThread().isInterrupted()) {
					req = getBinlogGetRequest(results, req);
					boolean needSend = isNeedSend(results, req);
					if (needSend) {
						req.getChannel().writeAndFlush(buildBinlogGetResponse(results, req));
						req = null;
					}

					Event binlogEvent = getEvent();
					saveBinlogEvent(results, binlogEvent);
					if (binlogEvent == null) {
						Thread.sleep(EMPTY_SLEEP_TIME);
					}
				}
			} catch (InterruptedException e) {
				LOG.info("AsyncTask has be Interrupted");
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				Cat.logError(e.getMessage(), e);
			}
		}

		protected BinlogGetResponse buildBinlogGetResponse(List<Event> results, BinlogGetRequest req) {
			BinlogGetResponse response = new BinlogGetResponse();
			response.setBinlogGetRequest(req);
			BinlogMessage message = new BinlogMessage();
			BinlogInfo lastBinlogInfo = null;

			Iterator<Event> iterator = results.iterator();
			while (iterator.hasNext() && message.getBinlogEvents().size() < req.getBatchSize()) {
				Event event = iterator.next();
				message.addBinlogEvents(event);
				if (event.getBinlogInfo() != null) {
					lastBinlogInfo = event.getBinlogInfo();
				}
				iterator.remove();
			}
			response.setBinlogMessage(message);

			SystemStatusManager.updateClientSendBinlogInfo(req.getClientName(), lastBinlogInfo);
			SystemStatusManager.addClientFetchQps(req.getClientName(), message.getBinlogEvents().size());
			return response;
		}

		protected boolean isNeedSend(List<Event> results, BinlogGetRequest req) {
			boolean needSend = false;
			if (req != null && (results.size() >= req.getBatchSize() || req.isTimeout())) {
				needSend = true;
			}
			return needSend;
		}

		protected void saveBinlogEvent(List<Event> results, Event binlogEvent) {
			if (binlogEvent != null) {
				results.add(binlogEvent);
			}
		}

		protected Event getEvent() {
			Event binlogEvent;
			try {
				binlogEvent = getParent().eventChannel.next(false);
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				binlogEvent = new ServerErrorEvent("get binlog event from storage failure.", e.getCause());
			}
			return binlogEvent;
		}

		protected BinlogGetRequest getBinlogGetRequest(List<Event> results, BinlogGetRequest req)
			throws InterruptedException {
			BinlogGetRequest request = req;

			if (!(request != null && request.getChannel().isActive())) {
				request = getParent().requests.poll();
			}

			if (request == null && results.size() >= CACHE_SIZE) {
				request = getParent().requests.take();
			}

			if (request != null && !threadNameHasSet) {
				threadNameHasSet = true;
				Thread.currentThread().setName("DefaultAsyncBinlogChannel-" + request.getClientName());
			}

			return request;
		}

		protected DefaultAsyncBinlogChannel getParent() throws InterruptedException {
			DefaultAsyncBinlogChannel channel = parent.get();
			if (channel == null) {
				LOG.warn("Parent has be GCed. Please check your code to call destroy.");
				Thread.currentThread().interrupt();
				throw new InterruptedException();
			}
			return channel;
		}
	}
}
