package com.dianping.puma.monitor.server;

import com.dianping.puma.config.DataConfig;
import com.dianping.puma.core.monitor.HeartbeatMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service("serverLaggingTimeMonitor")
public class ServerLaggingTimeMonitor {

	@Autowired
	DataConfig dataConfig;

	private HeartbeatMonitor heartbeatMonitor;

	private String title;

	private int delay;

	private int period;

	public ServerLaggingTimeMonitor() {}

	@PostConstruct
	public void init() {
		title = "ServerLagging.time";
		delay = 60;
		period = 60;
	}

	public void start() {
		heartbeatMonitor = new HeartbeatMonitor(title, delay, period);
		heartbeatMonitor.start();
	}

	public void stop() {
		heartbeatMonitor.stop();
	}

	public void record(String taskName, long execTime) {
		long diff = (System.currentTimeMillis() - execTime) / 1000;
		if (diff < dataConfig.getServerLaggingTimeThreshold()) {
			heartbeatMonitor.record(taskName, "0");
		} else {
			heartbeatMonitor.record(taskName, "1");
		}
	}
}
