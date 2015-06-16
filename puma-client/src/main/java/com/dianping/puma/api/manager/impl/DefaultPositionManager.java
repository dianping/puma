package com.dianping.puma.api.manager.impl;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.SpringContainer;
import com.dianping.puma.api.config.Config;
import com.dianping.puma.api.exception.PumaException;
import com.dianping.puma.api.manager.PositionManager;
import com.dianping.puma.api.service.PositionService;
import com.dianping.puma.api.service.impl.PigeonPositionService;
import com.dianping.puma.api.util.Clock;
import com.dianping.puma.core.model.BinlogInfo;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

public class DefaultPositionManager implements PositionManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultPositionManager.class);

	private boolean inited = true;

	private volatile BinlogInfo binlogInfo;

	private volatile long updateTime = 0;

	private int count = 0;

	private Timer timer = new Timer();

	private PumaClient client;

	private Config config;

	private Clock clock;

	private PositionService positionService;

	public DefaultPositionManager() {
	}

	@Override
	public void start() {
		if (inited) {
			logger.warn("Puma({}) position manager has been started already.", client.getName());
			return;
		}

		// Request binlog from pigeon service when starting.
		binlogInfo = request();

		// Setup the ack task.
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
		try {
			return request();
		} catch (Exception e) {
			String msg = String.format("Puma(%s) reading binlog from pigeon service error, use local instead: %s.", client.getName(), binlogInfo);
			logger.error(msg, e);
			Cat.logError(msg, e);

			return binlogInfo;
		}
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
		Pair<BinlogInfo, Long> pair = positionService.request(client.getName());
		BinlogInfo binlogInfo = pair.getLeft();
		if (binlogInfo == null) {
			throw new NullPointerException("Null binlog read from pigeon service.");
		}
		return binlogInfo;
	}

	private void ack() {
		String status = Message.SUCCESS;
		try {
			positionService.ack(client.getName(), Pair.of(binlogInfo, updateTime));
		} catch (Exception e) {
			status = "1";
			logger.warn("Puma({}) ack binlog({}) back to pigeon service error.", client.getName(), binlogInfo);
		} finally {
			Cat.logEvent("Puma", "client:ack", status, "");
		}
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

	public void setPositionService(PositionService positionService) {
		this.positionService = positionService;
	}

	private class AckWorker extends TimerTask {
		@Override
		public void run() {
			if (binlogInfo != null) {
				ack();
			}
		}
	}
}
