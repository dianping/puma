package com.dianping.puma.monitor.server;

import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.puma.core.monitor.HeartbeatMonitor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service("serverLaggingTimeMonitor")
public class ServerLaggingTimeMonitor {

	public static final String SERVER_LAGGING_TIME_THRESHOLD = "puma.server.serverlagging.time.threshold";

	private long serverLaggingTimeThreshold;

	private HeartbeatMonitor heartbeatMonitor;

	private String title;

	private int delay;

	private int period;

	public ServerLaggingTimeMonitor() {}

	@PostConstruct
	public void init() {
		serverLaggingTimeThreshold = ConfigCache.getInstance().getLongProperty(SERVER_LAGGING_TIME_THRESHOLD);

		ConfigCache.getInstance().addChange(new ConfigChange() {
			@Override
			public void onChange(String key, String value) {
				if (key.equals(SERVER_LAGGING_TIME_THRESHOLD)) {
					serverLaggingTimeThreshold = Long.parseLong(value);
				}
			}
		});

		title = "ServerLagging.time";
		delay = 60;
		period = 60;
		start();
	}

	public void start() {
		heartbeatMonitor = new HeartbeatMonitor(title, delay, period);
		heartbeatMonitor.start();
	}

	public void stop() {
		heartbeatMonitor.stop();
	}

	public void record(String taskName, long execTime) {
		heartbeatMonitor.record(taskName, genStatus(execTime));
	}

	public long getServerLaggingTimeThreshold() {
		return serverLaggingTimeThreshold;
	}

	public void setServerLaggingTimeThreshold(long serverLaggingTimeThreshold) {
		this.serverLaggingTimeThreshold = serverLaggingTimeThreshold;
	}

	private String genStatus(long execTime) {
		long diff = (System.currentTimeMillis() - execTime) / 1000;
		if (diff < serverLaggingTimeThreshold) {
			return "0";
		} else {
			return "1";
		}
	}
}
