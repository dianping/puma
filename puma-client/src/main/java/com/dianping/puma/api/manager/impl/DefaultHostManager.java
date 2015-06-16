package com.dianping.puma.api.manager.impl;

import com.dianping.cat.Cat;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.config.Config;
import com.dianping.puma.api.exception.PumaException;
import com.dianping.puma.api.manager.Feedback;
import com.dianping.puma.api.manager.HostManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DefaultHostManager implements HostManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultHostManager.class);

	private static final String HOSTS_KEY = "puma.client.hosts";

	private volatile boolean inited = false;

	private volatile List<String> hosts = new ArrayList<String>();
	private String host;
	private int index = 0;
	private int retries = 0;
	private Feedback state = Feedback.INITIAL;

	private PumaClient client;
	private Config config;
	private ConfigCache configCache;

	private ConfigChange configChange = new ConfigChange() {
		@Override
		public void onChange(String key, String value) {
			if (key.equalsIgnoreCase(localKey(HOSTS_KEY))) {
				try {
					changeHosts(value);

					// If current host not in the new host list, restart the subscribe thread.
					if (needToRestart()) {
						client.stop();
						client.start();
					}

				} catch (Exception e) {
					String msg = String.format("Puma changing hosts error.");
					PumaException pe = new PumaException(client.getName(), msg, e);
					logger.error(msg, pe);
					Cat.logError(msg, pe);
				}
			}
		}
	};

	public DefaultHostManager() {
	}

	@Override
	public void start() {
		if (inited) {
			logger.warn("Puma({}) host manager has been started already.", client.getName());
			return;
		}

		// Initialize hosts.
		changeHosts(configCache.getProperty(localKey(HOSTS_KEY)));

		// Register hosts listener.
		configCache.addChange(configChange);

		inited = true;
		logger.info("Puma({}) host manager has been started successfully.", client.getName());
	}

	@Override
	public void stop() {
		if (!inited) {
			logger.warn("Puma({}) host manager has been stopped already", client.getName());
			return;
		}

		// Unregister hosts listener.
		configCache.removeChange(configChange);

		// Frees.
		hosts.clear();
		hosts = null;

		inited = false;
		logger.info("Puma({}) host manager has been stopped successfully.", client.getName());
	}

	@Override
	public String next() {
		try {
			switch (state) {
			case INITIAL:
				retries = 0;
				host = rawHost();
				break;

			case SUCCESS:
				retries = 0;
				host = oriHost();
				break;

			case NET_ERROR:
				if (retries < config.getReconnectCount()) {
					++retries;
					host = oriHost();
				} else {
					retries = 0;
					host = newHost();
				}
				break;

			case SERVER_ERROR:
				retries = 0;
				host = newHost();
				break;

			default:
				throw new RuntimeException("Feeds back connection state before using host manager.");
			}
		} catch (Exception e) {
			String msg = String.format("Puma request host error.");
			PumaException pe = new PumaException(client.getName(), msg, e);
			logger.error(msg, pe);
			Cat.logError(msg, pe);
		}

		return host;
	}

	@Override
	public String current() {
		return host;
	}

	@Override
	public void feedback(Feedback state) {
		this.state = state;
	}

	private String rawHost() {
		index = 0;
		return hosts.get(index);
	}

	private String newHost() {
		index = (index + 1) % hosts.size();
		return hosts.get(index);
	}

	private String oriHost() {
		return host;
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
		if (state.equals(Feedback.INITIAL)) {
			return false;
		} else {
			return host == null || !hosts.contains(host);
		}
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

	public void setConfig(Config config) {
		this.config = config;
	}

	public void setConfigCache(ConfigCache configCache) {
		this.configCache = configCache;
	}
}
