package com.dianping.puma.channel.heartbeat;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.lion.client.LionException;
import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.event.HeartbeatEvent;
import com.dianping.puma.core.util.ByteArrayUtils;
import com.dianping.puma.monitor.MonitorScheduledExecutor;

public class HeartbeatTask implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(HeartbeatTask.class);

	private long initialDelay;
	private long interval;
	private TimeUnit unit;
	@SuppressWarnings("unchecked")
	private Future future;

	private HttpServletResponse response;

	private HeartbeatEvent event = null;

	private EventCodec codec = null;

	private static final String HEARTBEAT_INTERVAL_NAME = "puma.server.heartbeat.interval";

	public HeartbeatTask(EventCodec codec, HttpServletResponse response) {
		this.initialDelay = 0;
		this.unit = TimeUnit.MILLISECONDS;
		initConfig();
		event = new HeartbeatEvent();
		this.codec = codec;
		this.response = response;
	}

	public void initConfig() {
		this.setInterval(getLionInterval(HEARTBEAT_INTERVAL_NAME));
		ConfigCache.getInstance().addChange(new ConfigChange() {
			@Override
			public void onChange(String key, String value) {
				if (HEARTBEAT_INTERVAL_NAME.equals(key)) {
					HeartbeatTask.this.setInterval(Long.parseLong(value));
					if (future != null) {
						future.cancel(true);
						if (MonitorScheduledExecutor.instance.isScheduledValid()) {
							HeartbeatTask.this.execute();
						}
					}
				}
			}
		});
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

	@PostConstruct
	public void execute() {
		future = HeartbeatScheduledExecutor.instance.getExecutorService().scheduleWithFixedDelay(this,
				getInitialDelay(), getInterval(), getUnit());
	}

	@Override
	public void run() {
		if (response != null) {
			synchronized (response) {
				try {
					byte[] data = codec.encode(event);
					response.getOutputStream().write(ByteArrayUtils.intToByteArray(data.length));

					response.getOutputStream().write(data);
					response.getOutputStream().flush();
				} catch (IOException e) {
					Cat.getProducer().logError("puma.server.client.heartbeat.exception:", e);
					LOG.error("heartbeat.exception: ", e);
				}
			}
		}
	}

	private long getLionInterval(String intervalName) {
		long interval = 60000;
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
}
