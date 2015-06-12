package com.dianping.puma.api.manager;

import com.dianping.cat.Cat;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.exception.PumaClientConfigException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class HostManager {

	private static final Logger logger = LoggerFactory.getLogger(HostManager.class);

	private static final String PUMA_CLIENT_HOSTS_KEY = "";

	private volatile boolean inited = false;

	private volatile List<String> hosts;
	private int index = 0;
	private int retries = 0;
	private ConnectFeedback connectFeedback;

	private PumaClient client;
	private ConfigCache configCache;

	private ConfigChange configChange = new ConfigChange() {
		@Override
		public void onChange(String key, String value) {
			if (key.equalsIgnoreCase(PUMA_CLIENT_HOSTS_KEY)) {
				try {
					changeHosts(value);
				} catch (Exception e) {
					String msg = String.format("Puma client(%s) changing hosts error: (%s).", client.getName(), value);
					PumaClientConfigException pe = new PumaClientConfigException(msg, e);
					logger.error(msg, pe);
					Cat.logError(msg, pe);
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

		// Allocates.
		hosts = new ArrayList<String>();
		index = 0;
		retries = 0;
		connectFeedback = ConnectFeedback.INITIAL;

		// Initialize hosts.
		changeHosts(configCache.getProperty(PUMA_CLIENT_HOSTS_KEY));

		// Register hosts listener.
		configCache.addChange(configChange);

		inited = true;
	}

	public void stop() {
		if (!inited) {
			return;
		}

		// Unregister hosts listener.
		configCache.removeChange(configChange);

		// Frees.
		hosts.clear();
		hosts = null;

		inited = false;
	}

	public String next() {
		try {
			switch (connectFeedback) {
			case INITIAL:
				return hosts.get(index);
			case CONNECT_ERROR:
				if (retries < 0) {
					retries = 0;
					index = (index + 1) % hosts.size();
					return hosts.get(index);
				} else {
					++retries;
					return hosts.get(index);
				}
			case SERVER_ERROR:
				retries = 0;
				index = (index + 1) % hosts.size();
				return hosts.get(index);
			}
		} catch (Exception e) {
			String msg = String.format("Puma client(%s) ask for host error.", client.getName());
			PumaClientConfigException pe = new PumaClientConfigException(msg, e);
			logger.error(msg, pe);
			Cat.logError(msg, pe);
		}

		return null;
	}

	public void feedback(ConnectFeedback connectFeedback) {
		this.connectFeedback = connectFeedback;
	}

	private void changeHosts(String hostStr) {
		hosts.clear();
		String[] hostArray = StringUtils.split(StringUtils.normalizeSpace(hostStr), ",");
		if (hostArray == null) {
			throw new NullPointerException("Hosts is null.");
		} else {
			hosts.addAll(Arrays.asList(hostArray));
		}
	}

	public void setClient(PumaClient client) {
		this.client = client;
	}

	public void setConfigCache(ConfigCache configCache) {
		this.configCache = configCache;
	}

	public enum ConnectFeedback {
		INITIAL,
		CONNECT_ERROR,
		SERVER_ERROR,
	}

}
