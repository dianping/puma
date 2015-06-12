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

	private static final String HOSTS_KEY = "puma.client.hosts";

	private volatile boolean inited = false;

	private volatile List<String> hosts;
	private String host;
	private int index;
	private int retries;
	private ConnectFeedback connectFeedback;

	private PumaClient client;
	private ConfigCache configCache;

	private ConfigChange configChange = new ConfigChange() {
		@Override
		public void onChange(String key, String value) {
			if (key.equalsIgnoreCase(localKey(HOSTS_KEY))) {
				try {
					changeHosts(value);

					// If current host not in the new host list, restart the subscribe thread.
					if (needToRestart()) {
						client.restartSubscribe();
					}

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
		host = "0.0.0.0";
		index = 0;
		retries = 0;
		connectFeedback = ConnectFeedback.INITIAL;

		// Initialize hosts.
		changeHosts(configCache.getProperty(localKey(HOSTS_KEY)));

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
		host = null;

		try {
			switch (connectFeedback) {
			case INITIAL:
				host = hosts.get(index);
				break;
			case CONNECT_ERROR:
				if (retries < 0) {
					retries = 0;
					index = (index + 1) % hosts.size();
					host = hosts.get(index);
				} else {
					++retries;
				}
				break;
			case SERVER_ERROR:
				retries = 0;
				index = (index + 1) % hosts.size();
				host = hosts.get(index);
				break;
			}
		} catch (Exception e) {
			String msg = String.format("Puma client(%s) ask for host error.", client.getName());
			PumaClientConfigException pe = new PumaClientConfigException(msg, e);
			logger.error(msg, pe);
			Cat.logError(msg, pe);
		}

		return host;
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

	private boolean needToRestart() {
		return host == null || !hosts.contains(host);
	}

	private String localKey(String key) {
		return (new StringBuilder())
				.append(key)
				.append(".")
				.append(client.getName())
				.toString();
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
