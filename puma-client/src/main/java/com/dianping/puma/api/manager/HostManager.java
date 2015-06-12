package com.dianping.puma.api.manager;

import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.config.Config;
import com.dianping.puma.api.exception.PumaClientConfigException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class HostManager {

	private static final Logger logger = LoggerFactory.getLogger(HostManager.class);

	private static final String PUMA_CLIENT_HOSTS_KEY = "";

	private volatile boolean inited = false;

	private PumaClient client;

	private String[] hosts;

	private String host;

	private int index = 0;

	private int retries = 0;

	private Config config;

	private ConfigCache configCache;

	private ConfigChange configChange = new ConfigChange() {
		@Override
		public void onChange(String key, String value) {
			if (key.equalsIgnoreCase(PUMA_CLIENT_HOSTS_KEY)) {
				try {
					changeHosts(value);
				} catch (PumaClientConfigException e) {
					String msg = String.format("Puma client(%s) changing hosts error: (%s).", client.getName(), value);
					throw e;
				}
			}
		}
	};

	public HostManager() {

	}

	public void start() {
		if (inited) {
			return;
		}

		// Initialize hosts.
		changeHosts(ConfigCache.getInstance().getProperty(PUMA_CLIENT_HOSTS_KEY));

		// Register hosts listener.
		ConfigCache.getInstance().addChange(configChange);

		inited = true;
	}

	public void stop() {
		if (!inited) {
			return;
		}

		// Unregister hosts listener.
		ConfigCache.getInstance().removeChange(configChange);

		inited = false;
	}

	private void changeHosts(String hostStr) throws PumaClientConfigException {
		if (hostStr == null) {
			String msg = String.format("Puma client(%s) null hosts.", client.getName());
			throw new PumaClientConfigException(msg);
		} else {
			hostStr = StringUtils.normalizeSpace(hostStr);
			hosts = StringUtils.split(hostStr, ",");
		}
	}

	public String next() {
		if (retries > config.getReconnectTimes()) {
			// Change a server to connect.
			retries = 0;

			index = (index + 1) % hosts.length;
			String currentHost = hosts[index];
			host = (currentHost == null) ? host : currentHost;
			return host;
		} else {
			return host;
		}
	}

	public void feedback(boolean success) {
		if (!success) {
			++retries;
		}
	}

	public void setClient(PumaClient client) {
		this.client = client;
	}

	public void setConfigCache(ConfigCache configCache) {
		this.configCache = configCache;
	}

	public void setConfig(Config config) {
		this.config = config;
	}
}
