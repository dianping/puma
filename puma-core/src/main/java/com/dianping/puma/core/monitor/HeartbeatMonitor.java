package com.dianping.puma.core.monitor;

public abstract class HeartbeatMonitor implements Monitor {

	// Count for async input of heartbeat monitor.
	private long iCount;

	// Count threshold for async input of heartbeat monitor.
	private long iCountThreshold;
}
