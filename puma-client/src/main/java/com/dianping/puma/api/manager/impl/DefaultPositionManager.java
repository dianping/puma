package com.dianping.puma.api.manager.impl;

import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.config.Config;
import com.dianping.puma.api.manager.PositionManager;
import com.dianping.puma.api.util.Clock;
import com.dianping.puma.core.model.BinlogInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

public class DefaultPositionManager implements PositionManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultPositionManager.class);

	private boolean inited = true;

	private volatile BinlogInfo binlogInfo = null;
	private volatile long updateTime = 0;
	private int count = 0;
	private Timer timer = new Timer();

	private PumaClient client;
	private Config config;
	private Clock clock;

	public DefaultPositionManager() {
	}

	@Override
	public void start() {
		if (inited) {
			logger.warn("Puma({}) position manager has been started already.", client.getName());
			return;
		}

		timer.scheduleAtFixedRate(new AckWorker(), 0, config.getBinlogAckTime());

		inited = true;
		logger.info("Puma({}) position manager has been started successfully.", client.getName());
	}

	@Override
	public void stop() {
		if (!inited) {
			logger.warn("Puma({}) position manager has been stopped already.", client.getName());
			return;
		}

		timer.cancel();
		timer = null;

		inited = false;
		logger.info("Puma({}) position manager has been stopped successfully.", client.getName());
	}

	@Override
	public BinlogInfo next() {
		if (binlogInfo == null || (clock.getCurrentTime() - updateTime) > config.getBinlogExpiredTime()) {
			request();
		}

		return binlogInfo;
	}

	@Override
	public void feedback(BinlogInfo binlogInfo) {
		this.binlogInfo = binlogInfo;
		this.updateTime = clock.getCurrentTime();

		++count;
		if (count >= config.getBinlogAckCount()) {
			count = 0;
			ack();
		}
	}

	private BinlogInfo request() {
		return null;
	}

	private void ack() {

	}

	public void setClient(PumaClient client) {
		this.client = client;
	}

	public void setConfig(Config config) {
		this.config = config;
	}

	public void setClock(Clock clock) {
		this.clock = clock;
	}

	private class AckWorker extends TimerTask {
		@Override
		public void run() {
			if (binlogInfo != null && (clock.getCurrentTime() - updateTime) < config.getBinlogExpiredTime()) {
				ack();
			}
		}
	}
}
