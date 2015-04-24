package com.dianping.puma.api;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.lion.client.LionException;
import com.dianping.puma.core.util.ScheduledExecutorUtils;

public class HeartbeatListener {

	private static final Logger LOG = LoggerFactory.getLogger(HeartbeatListener.class);

	private static final String THREAD_FACTORY_NAME = "heartbeat";

	private static final String HEARTBEAT_CHECKER_INTERVAL_NAME = "puma.client.heartbeatchecker.interval";

	private ScheduledExecutorService executorService;

	private long initialDelay;

	private long interval;

	private TimeUnit unit;

	@SuppressWarnings("unchecked")
	private Future future;

	private PumaClient pumaClient;

	public HeartbeatListener(PumaClient pumaClient) {
		this.interval = initInterval();
		this.initialDelay = this.interval;
		this.pumaClient = pumaClient;
		this.unit = TimeUnit.MILLISECONDS;
		this.executorService = ScheduledExecutorUtils.createSingleScheduledExecutorService(THREAD_FACTORY_NAME);
	}

	public long initInterval() {
		long interval = getLionInterval(HEARTBEAT_CHECKER_INTERVAL_NAME);
		ConfigCache.getInstance().addChange(new ConfigChange() {
			@Override
			public void onChange(String key, String value) {
				if (HEARTBEAT_CHECKER_INTERVAL_NAME.equals(key)) {
					HeartbeatListener.this.setInterval(Long.parseLong(value));
					if (future != null) {
						future.cancel(true);
						if (HeartbeatListener.this.isScheduledValid()) {
							HeartbeatListener.this.start();
						}
					}
				}
			}
		});
		return interval;
	}

	public void start() {
		future = getExecutorService().scheduleWithFixedDelay(new HeartbeatListenTask(), getInitialDelay(),
				getInterval(), getUnit());
	}

	public void stop() {
		if (future != null && !future.isCancelled() && !future.isDone()) {
			future.cancel(true);
		}
	}

	private long getLionInterval(String intervalName) {
		long interval = 180000;
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

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public long getInterval() {
		return interval;
	}

	public void setInitialDelay(long initialDelay) {
		this.initialDelay = initialDelay;
	}

	public long getInitialDelay() {
		return initialDelay;
	}

	public void setUnit(TimeUnit unit) {
		this.unit = unit;
	}

	public TimeUnit getUnit() {
		return unit;
	}

	private boolean isScheduledValid() {
		if (getExecutorService() != null && !getExecutorService().isShutdown() && !getExecutorService().isTerminated()) {
			return true;
		}
		return false;
	}

	private ScheduledExecutorService getExecutorService() {
		return executorService;
	}

	private class HeartbeatListenTask implements Runnable {

		@Override
		public void run() {
			if (pumaClient.isHasHeartbeat()) {
				Log.info("pumaClient receive heartbeat. reset heart beat mark.");
				pumaClient.setHasHeartbeat(false);
			} else {
				Log.info("puma client no receive heartbeat. restart pumaClient.");
				pumaClient.stop();
				pumaClient.start();
			}
		}

	}
}
