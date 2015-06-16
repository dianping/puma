package com.dianping.puma.api.manager.impl;

import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.config.Config;
import com.dianping.puma.api.manager.HeartbeatManager;
import com.dianping.puma.api.util.Clock;
import com.dianping.puma.api.util.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

public class DefaultHeartbeatManager implements HeartbeatManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultHeartbeatManager.class);

	private boolean inited = false;

	private volatile boolean marked = false;
	private volatile long last;
	private Timer timer = new Timer();

	private PumaClient client;
	private Monitor monitor;
	private Config config;
	private Clock clock;

	public void start() {
		if (inited) {
			logger.warn("Puma({}) heartbeat manager has been started already.", client.getName());
			return;
		}

		timer.scheduleAtFixedRate(new HeartbeatCheckTask(), 0, config.getHeartbeatCheckTime());

		inited = true;
		logger.info("Puma({}) heartbeat manager has been started successfully.", client.getName());
	}

	public void stop() {
		if (!inited) {
			logger.warn("Puma({}) heartbeat manager has been stopped already.", client.getName());
			return;
		}

		timer.cancel();
		timer = null;

		inited = false;
		logger.info("Puma({}) heartbeat manager has been stopped successfully.", client.getName());
	}

	public void open() {
		marked = true;
		heartbeat();

		logger.info("Puma({}) heartbeat manager has been opened successfully.", client.getName());
	}

	public void close() {
		marked = false;

		logger.info("Puma({}) heartbeat manager has been closed successfully.", client.getName());
	}

	public void heartbeat() {
		last = clock.getCurrentTime();
	}

	private boolean expired() {
		return (clock.getCurrentTime() - last) >= config.getHeartbeatExpiredTime() * 1000;
	}

	public void setClient(PumaClient client) {
		this.client = client;
	}

	public void setMonitor(Monitor monitor) {
		this.monitor = monitor;
	}

	public void setConfig(Config config) {
		this.config = config;
	}

	public void setClock(Clock clock) {
		this.clock = clock;
	}

	private class HeartbeatCheckTask extends TimerTask {

		@Override
		public void run() {
			if (marked && expired()) {
				monitor.logError(logger, "heartbeat expired");

				// Restart the client.
				client.stop();
				client.start();
			}
		}
	}
}
