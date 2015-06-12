package com.dianping.puma.api.config;

import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.puma.api.PumaClient;

public class GlobalConfig {

	private static final String RECONNECT_SLEEP_TIME_KEY = "puma.client.reconnect.sleep.time";
	private static final String RECONNECT_TIMES_KEY = "puma.client.reconnect.times";

	private boolean inited = false;

	private long reconnectSleepTime;
	private long reconnectTimes;

	private ConfigChange configChange = new ConfigChange() {
		@Override
		public void onChange(String key, String value) {
			if (key.equalsIgnoreCase(RECONNECT_SLEEP_TIME_KEY)) {
				reconnectSleepTime = Long.valueOf(value);
			} else if (key.equalsIgnoreCase(RECONNECT_TIMES_KEY)) {
				reconnectTimes = Long.valueOf(value);
			}
		}
	};

	private PumaClient client;
	private ConfigCache configCache;

	public void start() {
		if (inited) {
			return;
		}

		reconnectSleepTime = configCache.getLongProperty(RECONNECT_SLEEP_TIME_KEY);
		reconnectTimes = configCache.getLongProperty(RECONNECT_TIMES_KEY);

		configCache.addChange(configChange);

		inited = true;
	}

	public void stop() {
		if (!inited) {
			return;
		}

		configCache.removeChange(configChange);

		inited = false;
	}

	public long getReconnectSleepTime() {
		return reconnectSleepTime;
	}

	public long getReconnectTimes() {
		return reconnectTimes;
	}

	public void setClient(PumaClient client) {
		this.client = client;
	}

	public void setConfigCache(ConfigCache configCache) {
		this.configCache = configCache;
	}
}
