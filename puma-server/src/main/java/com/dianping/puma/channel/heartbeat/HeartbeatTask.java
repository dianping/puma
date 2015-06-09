package com.dianping.puma.channel.heartbeat;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.lion.client.LionException;
import com.dianping.puma.channel.exception.HeartbeatSenderException;
import com.dianping.puma.common.SystemStatusContainer;
import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.event.HeartbeatEvent;
import com.dianping.puma.core.util.ByteArrayUtils;
import com.dianping.puma.monitor.ServerEventDelayMonitor;

public class HeartbeatTask {

	private static final Logger LOG = LoggerFactory.getLogger(HeartbeatTask.class);

	private static final String HEARTBEAT_SENDER_INTERVAL_NAME = "puma.server.heartbeatsender.interval";
	private String clientName;
	private long initialDelay;
	private long interval;
	private TimeUnit unit;

	private Future future;

	private HttpServletResponse response;

	private HeartbeatEvent event = null;

	private EventCodec codec = null;

	private ScheduledExecutorService executorService = null;

	private ServerEventDelayMonitor serverEventDelayMonitor;

	private Lock lock;

	public HeartbeatTask(EventCodec codec, HttpServletResponse response, String clientName, Lock lock,
			ServerEventDelayMonitor serverEventDelayMonitor) {
		this.clientName = clientName;
		this.initialDelay = 0;
		this.unit = TimeUnit.MILLISECONDS;
		initConfig();
		event = new HeartbeatEvent();
		this.codec = codec;
		this.response = response;
		this.lock = lock;
		executorService = HeartbeatScheduledExecutor.instance.getExecutorService();
		execute();
		this.serverEventDelayMonitor = serverEventDelayMonitor;
		LOG.info("puma server HeartbeatTask constructed.");
	}

	public void initConfig() {
		this.setInterval(getLionInterval(HEARTBEAT_SENDER_INTERVAL_NAME));
		ConfigCache.getInstance().addChange(new ConfigChange() {
			@Override
			public void onChange(String key, String value) {
				if (HEARTBEAT_SENDER_INTERVAL_NAME.equals(key)) {
					HeartbeatTask.this.setInterval(Long.parseLong(value));
					if (HeartbeatTask.this.isFutureValid()) {
						future.cancel(true);
						if (HeartbeatScheduledExecutor.instance.isExecutorServiceValid()) {
							HeartbeatTask.this.execute();
						}
					}
				}
			}
		});
	}

	public boolean isFutureValid() {
		if (getFuture() != null && !getFuture().isCancelled()) {
			return true;
		}
		return false;
	}

	public void cancelFuture() {
		if (isFutureValid()) {
			getFuture().cancel(true);
		}
	}

	public void execute() {
		future = executorService.scheduleWithFixedDelay(new HeartbeatSender(), getInitialDelay(), getInterval(),
				getUnit());
	}

	private long getLionInterval(String intervalName) {
		long interval = 30000;
		try {
			Long temp = ConfigCache.getInstance().getLongProperty(intervalName);
			if (temp != null) {
				interval = temp.longValue();
			}
		} catch (LionException e) {
			LOG.error(e.getMessage(), e);
		}
		return interval;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getClientName() {
		return clientName;
	}

	private class HeartbeatSender implements Runnable {
		@Override
		public void run() {
			if (response != null) {

				try {
					byte[] data = codec.encode(event);
					if (lock.tryLock(60, TimeUnit.SECONDS)) {
						try {
							response.getOutputStream().write(ByteArrayUtils.intToByteArray(data.length));
							response.getOutputStream().write(data);
							response.getOutputStream().flush();
						} finally {
							lock.unlock();
						}
					} else {
						throw new IOException("Client obtain write changedEvent lock failed.");
					}
					LOG.info(HeartbeatTask.this.clientName + " puma server heartbeat sended.");
					Cat.logEvent("ClientConnect.heartbeated", HeartbeatTask.this.clientName, Message.SUCCESS, "");
				} catch (InterruptedException e) {
					LOG.warn(HeartbeatTask.this.clientName + " puma server heartbeat interrupted.");
				} catch (IOException e) {
					HeartbeatTask.this.cancelFuture();
					try {
						response.getOutputStream().close();
					} catch (IOException e1) {
						// ignore
					}
					SystemStatusContainer.instance.removeClient(HeartbeatTask.this.clientName);
					serverEventDelayMonitor.remove(clientName);
					Cat.logEvent("ClientConnect.heartbeated", HeartbeatTask.this.clientName, "1", "");
					Cat.logError("ClientConnect.heartbeated.closed: ", new HeartbeatSenderException(
							"ClientConnect.heartbeated.closed: " + HeartbeatTask.this.clientName, e));
					LOG.error("ClientConnect.heartbeated.closed: ClientName = " + HeartbeatTask.this.clientName, e);

				}
			}
		}

	}

	public void setInitialDelay(long initialDelay) {
		this.initialDelay = initialDelay;
	}

	public long getInitialDelay() {
		return initialDelay;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public long getInterval() {
		return interval;
	}

	public void setUnit(TimeUnit unit) {
		this.unit = unit;
	}

	public TimeUnit getUnit() {
		return unit;
	}

	@SuppressWarnings("rawtypes")
	public Future getFuture() {
		return this.future;
	}

	public void setFuture(@SuppressWarnings("rawtypes") Future future) {
		this.future = future;
	}

	public ServerEventDelayMonitor getServerEventDelayMonitor() {
		return serverEventDelayMonitor;
	}

	public void setServerEventDelayMonitor(ServerEventDelayMonitor serverEventDelayMonitor) {
		this.serverEventDelayMonitor = serverEventDelayMonitor;
	}

}
