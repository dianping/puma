package com.dianping.puma.api.manager.impl;

import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.manager.LockManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultLockManager implements LockManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultLockManager.class);

	private boolean inited = false;

	private boolean lock = false;
	private long lockTime = 0;

	private PumaClient client;

	@Override
	public void start() {
		if (inited) {
			logger.warn("Puma({}) lock manager has been started already.", client.getName());
			return;
		}

		inited = true;
		logger.info("Puma({}) lock manager has been started successfully.", client.getName());
	}

	@Override
	public void stop() {
		if (!inited) {
			logger.warn("Puma({}) lock manager has been stopped already.", client.getName());
			return;
		}

		inited = false;
		logger.info("Puma({}) lock manager has been stopped successfully.", client.getName());
	}

	@Override
	public boolean tryLock() {
		return true;
	}

	@Override
	public void unlock() {

	}

	public void setClient(PumaClient client) {
		this.client = client;
	}
}
