package com.dianping.puma.api.config;

import com.dianping.cat.Cat;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.exception.PumaClientConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalConfig {

	private static final Logger logger = LoggerFactory.getLogger(LocalConfig.class);

	private static final String TARGET_KEY = "puma.client.target";
	private static final String SERVER_ID_KEY = "puma.client.serverid";

	private boolean inited = false;

	private volatile String target;
	private volatile long serverId;

	private PumaClient client;
	private ConfigCache configCache;

	private ConfigChange configChange = new ConfigChange() {
		@Override
		public void onChange(String key, String value) {
			try {
				if (key.equalsIgnoreCase(localKey(TARGET_KEY))) {
					target = String.valueOf(value);
				} else if (key.equalsIgnoreCase(localKey(SERVER_ID_KEY))) {
					serverId = Long.valueOf(value);
				}
			} catch (Exception e) {
				String msg = String.format("Puma client(%s) change local config error.", client.getName());
				PumaClientConfigException pe = new PumaClientConfigException(msg, e);
				logger.error(msg, pe);
				Cat.logError(msg, pe);
			}
		}
	};

	public void start() {
		if (inited) {
			return;
		}

		target = configCache.getProperty(localKey(TARGET_KEY));
		serverId = configCache.getLongProperty(localKey(SERVER_ID_KEY));

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

	private String localKey(String key) {
		return (new StringBuilder())
				.append(key)
				.append(".")
				.append(client.getName())
				.toString();
	}

	public String getTarget() {
		return target;
	}

	public long getServerId() {
		return serverId;
	}

	public void setClient(PumaClient client) {
		this.client = client;
	}

	public void setConfigCache(ConfigCache configCache) {
		this.configCache = configCache;
	}
}
