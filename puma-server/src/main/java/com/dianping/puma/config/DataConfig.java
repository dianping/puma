package com.dianping.puma.config;

import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service("dataConfig")
public class DataConfig implements InitializingBean {

	public static final String SERVER_LAGGING_TIME_THRESHOLD = "puma.server.serverlagging.time.threshold";

	private long serverLaggingTimeThreshold;

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
	}

	public long getServerLaggingTimeThreshold() {
		return serverLaggingTimeThreshold;
	}

	public void setServerLaggingTimeThreshold(long serverLaggingTimeThreshold) {
		this.serverLaggingTimeThreshold = serverLaggingTimeThreshold;
	}

	@Override
	public void afterPropertiesSet() {}
}
