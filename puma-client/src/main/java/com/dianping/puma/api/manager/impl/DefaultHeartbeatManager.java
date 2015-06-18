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

	private volatile boolean closed = true;
	private volatile long last;
	private Timer timer;

	private PumaClient client;
	private Monitor monitor;
	private Config config;
	private Clock clock;

	public void start() {
		if (inited) {
			return;
		}

		timer = new Timer(String.format("puma-heartbeat-thread-%s", client.getName()));
		timer.scheduleAtFixedRate(new HeartbeatCheckTask(), 0, config.getHeartbeatCheckTime());

		inited = true;
	}

	public void stop() {
		if (!inited) {
			return;
		}

		timer.cancel();

		inited = false;
	}

	public void open() {
		closed = false;
		heartbeat();

		monitor.logInfo(logger, "heartbeat open");
	}

	public void close() {
		closed = true;

		monitor.logInfo(logger, "heartbeat close");
	}

	public void heartbeat() {
		last = clock.getCurrentTime();
	}

	private boolean expired() {
		return (clock.getCurrentTime() - last) >= config.getHeartbeatExpiredTime();
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
			if (!closed && expired()) {
				monitor.logError(logger, "heartbeat expired");

				client.stopSubscribe();
				client.startSubscribe();
			}
		}
	}
}
