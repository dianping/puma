package com.dianping.puma.api.manager;

import com.dianping.cat.Cat;
import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.exception.PumaClientConnectException;
import com.dianping.puma.api.util.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

public class HeartbeatManager {

	private static final Logger logger = LoggerFactory.getLogger(HeartbeatManager.class);

	private PumaClient client;
	private Clock clock;
	private Timer timer = new Timer();

	private long last;

	public void start() {
		heartbeat();
		timer.scheduleAtFixedRate(new HeartbeatCheckTask(), 0, 0);
	}

	public void stop() {
		timer.cancel();
		timer = null;
	}

	public void heartbeat() {
		last = clock.getCurrentTime();
	}

	private boolean expired() {
		return (clock.getCurrentTime() - last) >= 0 * 1000;
	}

	private class HeartbeatCheckTask extends TimerTask {

		@Override
		public void run() {
			if (expired()) {
				String msg = String.format("Puma client(%s) heartbeat expired.", client.getName());
				PumaClientConnectException pe = new PumaClientConnectException(msg);
				logger.error(msg, pe);
				Cat.logError(msg, pe);

				// Reset heartbeat and restart subscribe thread if heartbeat is expired.
				heartbeat();
				client.restartSubscribe();
			}
		}
	}

	public void setClient(PumaClient client) {
		this.client = client;
	}

	public void setClock(Clock clock) {
		this.clock = clock;
	}
}
