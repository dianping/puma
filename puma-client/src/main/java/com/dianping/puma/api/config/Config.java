package com.dianping.puma.api.config;

public class Config {

	public void start() {

	}

	public void stop() {

	}

	public static Config getInstance() {
		return new Config();
	}

	private long reconnectSleepTime = 5000; // 5s.
	private boolean localReconnectSleepTime = false;

	public long getReconnectSleepTime() {
		return 0L;
	}

	public long getReconnectTimes() {
		return 0L;
	}

	public long getAckInterval() {
		return 0L;
	}

	public long getHeartbeatCheckTime() {
		return 0L;
	}

	public long getHeartbeatExpiredTime() {
		return 0L;
	}
}
