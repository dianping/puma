package com.dianping.puma.api;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.lion.client.LionException;
import com.dianping.puma.core.util.ScheduledExecutorUtils;

public class ClientAckScheduledService {

	private static final Logger LOG = LoggerFactory.getLogger(ClientAckScheduledService.class);

	private static final String SERVER_ACK_INTERVAL = "puma.client.clientack.interval";

	private static final String FACTORY_NAME = "serverAck";

	@Autowired
	private ClientAckService clientAckService;

	private volatile long interval = 5000L;

	private ScheduledExecutorService executorService = null;

	private ScheduledFuture scheduledFuture;

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public ScheduledFuture getScheduledFuture() {
		return this.scheduledFuture;
	}

	public void setScheduledFuture(ScheduledFuture scheduledFuture) {
		this.scheduledFuture = scheduledFuture;
	}

	public void setScheduledExecutorService(ScheduledExecutorService executorService) {
		this.executorService = executorService;
	}

	public ScheduledExecutorService getScheduledExecutorService() {
		return this.executorService;
	}

	private boolean isScheduledFutureValid() {
		if (getScheduledFuture() != null && !getScheduledFuture().isCancelled()) {
			return true;
		}
		return false;
	}

	private boolean isExecutorServiceValid() {
		if (getScheduledExecutorService() != null && !getScheduledExecutorService().isShutdown()) {
			return true;
		}
		return false;
	}

	public ClientAckScheduledService() {
		initConfig();

		executorService = ScheduledExecutorUtils.createSingleScheduledExecutorService(FACTORY_NAME);

		execute();
	}

	public void initConfig() {
		try {
			this.setInterval(ConfigCache.getInstance().getLongProperty(SERVER_ACK_INTERVAL));
			ConfigCache.getInstance().addChange(new ConfigChange() {
				@Override
				public void onChange(String key, String value) {
					if (SERVER_ACK_INTERVAL.equals(key)) {
						setInterval(Long.parseLong(value));
						if (isScheduledFutureValid()) {
							getScheduledFuture().cancel(true);
							if (isExecutorServiceValid()) {
								execute();
							}
						}
					}
				}
			});
		} catch (LionException e) {
			LOG.error(SERVER_ACK_INTERVAL +" Lion config read exception, Reason: ", e.getMessage());
		}
	}

	public void execute() {
		scheduledFuture = executorService.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				clientAckService.pushClientAck();
			}
		}, 0, getInterval(), TimeUnit.MILLISECONDS);
	}

}