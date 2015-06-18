package com.dianping.puma.api.manager.impl;

import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.config.Config;
import com.dianping.puma.api.manager.HostManager;
import com.dianping.puma.api.manager.PositionManager;
import com.dianping.puma.api.service.PositionService;
import com.dianping.puma.api.util.Clock;
import com.dianping.puma.api.util.Monitor;
import com.dianping.puma.core.model.BinlogInfo;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

public class DefaultPositionManager implements PositionManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultPositionManager.class);

	private boolean inited = false;

	private volatile BinlogInfo binlogInfo;
	private volatile long updateTime = 0;
	private int count = 0;
	private Timer timer;

	private PumaClient client;
	private Monitor monitor;
	private HostManager hostManager;
	private Config config;
	private Clock clock;
	private PositionService positionService;

	public DefaultPositionManager() {
	}

	@Override
	public void start() {
		if (inited) {
			return;
		}

		binlogInfo = request();

		timer = new Timer(String.format("puma-position-thread-%s", client.getName()));
		timer.scheduleAtFixedRate(new AckWorker(), 0, config.getBinlogAckTime());

		inited = true;
	}

	@Override
	public void stop() {
		if (!inited) {
			return;
		}

		timer.cancel();
		timer = null;

		inited = false;
	}

	@Override
	public BinlogInfo next() {
		try {
			return request();
		} catch (Exception e) {
			String msg = String.format("reading remote binlog error, use local instead: %s.", binlogInfo);
			monitor.logError(logger, hostManager.current(), msg);
			return binlogInfo;
		}
	}

	@Override
	public void save(BinlogInfo binlogInfo) {
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
		return (pair == null) ? null : pair.getLeft();
	}

	private void ack() {
		try {

			if (binlogInfo != null) {
				positionService.ack(client.getName(), Pair.of(binlogInfo, updateTime));
				monitor.logInfo(logger, hostManager.current(), "ack");
			}

		} catch (Throwable e) {
			monitor.logError(logger, hostManager.current(), "ack error", e);
		}
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

	public void setHostManager(HostManager hostManager) {
		this.hostManager = hostManager;
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
			ack();
		}
	}
}
