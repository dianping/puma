package com.dianping.puma.channel.heartbeat;

import java.util.concurrent.ScheduledExecutorService;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.dianping.puma.core.util.ScheduledExecutorUtils;

@Component("heartbeatScheduledExecutor")
public class HeartbeatScheduledExecutor implements InitializingBean {

	public static HeartbeatScheduledExecutor instance;

	private ScheduledExecutorService executorService = null;

	private static final int MAX_THREAD_COUNT = 2;

	private static final String THREAD_FACTORY_NAME = "heartbeat";

	public HeartbeatScheduledExecutor() {
		setExecutorService(ScheduledExecutorUtils.createScheduledExecutorService(MAX_THREAD_COUNT, THREAD_FACTORY_NAME));
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		instance = this;
	}

	public void setExecutorService(ScheduledExecutorService executorService) {
		this.executorService = executorService;
	}

	public ScheduledExecutorService getExecutorService() {
		return executorService;
	}

	public boolean isExecutorServiceValid() {
		if (getExecutorService() != null && !getExecutorService().isShutdown() && !getExecutorService().isTerminated()) {
			return true;
		}
		return false;
	}
}
