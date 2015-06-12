package com.dianping.puma.api.manager;

import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.puma.core.model.BinlogInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PositionManager {

	private static final Logger logger = LoggerFactory.getLogger(PositionManager.class);

	private volatile boolean stopped = true;

	private String name;

	private BinlogInfo binlogInfo;

	public PositionManager() {
	}

	public void start() {
		if (!stopped) {
			return;
		}
		stopped = true;

		AckWorker ackWorker = new AckWorker();

	}

	public void stop() {

	}

	public BinlogInfo next() {
		return new BinlogInfo();
	}

	public void save(BinlogInfo binlogInfo) {

	}

	public void setName(String name) {
		this.name = name;
	}

	private class AckWorker implements Runnable {

		@Override
		public void run() {
			// @TODO: Call the pigeon service.
		}
	}
}
