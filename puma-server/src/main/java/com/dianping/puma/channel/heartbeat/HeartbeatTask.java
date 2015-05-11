package com.dianping.puma.channel.heartbeat;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.lion.client.LionException;
import com.dianping.puma.ComponentContainer;
import com.dianping.puma.common.SystemStatusContainer;
import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.event.HeartbeatEvent;
import com.dianping.puma.core.model.container.client.ClientStateContainer;
import com.dianping.puma.core.util.ByteArrayUtils;

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

	@Autowired
	private HeartbeatScheduledExecutor heartbeatScheduledExecutor;

	public HeartbeatTask(EventCodec codec, HttpServletResponse response, String clientName) {
		this.clientName = clientName;
		this.initialDelay = 0;
		this.unit = TimeUnit.MILLISECONDS;
		initConfig();
		event = new HeartbeatEvent();
		this.codec = codec;
		this.response = response;
		executorService = HeartbeatScheduledExecutor.instance.getExecutorService();
		execute();
		Log.info("puma server HeartbeatTask constructed.");
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
		if (getFuture() != null && !getFuture().isCancelled() && !getFuture().isDone()) {
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
				synchronized (response) {
					try {
						byte[] data = codec.encode(event);
						response.getOutputStream().write(ByteArrayUtils.intToByteArray(data.length));

						response.getOutputStream().write(data);
						response.getOutputStream().flush();
						LOG.info(HeartbeatTask.this.clientName + " puma server heartbeat sended.");
						Cat.logEvent("ClientConnect.heartbeated", HeartbeatTask.this.clientName, Message.SUCCESS, "");
					} catch (IOException e) {
						HeartbeatTask.this.cancelFuture();
						try {
							response.getOutputStream().close();
						} catch (IOException e1) {
							// ignore
						}
						ClientStateContainer clientStateContainer = ComponentContainer.SPRING
								.lookup("clientStateContainer");
						clientStateContainer.remove(HeartbeatTask.this.clientName);
						SystemStatusContainer.instance.removeClient(HeartbeatTask.this.clientName);
						Cat.logEvent("ClientConnect.heartbeated", HeartbeatTask.this.clientName, "1", "");
						Cat.logError("ClientConnect.heartbeated.closed: ", e);
						LOG.error("ClientConnect.heartbeated.closed: ", e);
					}
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

}
