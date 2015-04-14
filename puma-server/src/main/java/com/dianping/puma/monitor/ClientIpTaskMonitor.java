package com.dianping.puma.monitor;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.puma.common.SystemStatusContainer;
import com.dianping.puma.common.SystemStatusContainer.ClientStatus;

public class ClientIpTaskMonitor extends AbstractTaskMonitor implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(ClientIpTaskMonitor.class);

	public ClientIpTaskMonitor(long initialDelay, long period, TimeUnit unit) {
		super(initialDelay, period, unit);
		LOG.info("ClientIp Task Monitor started.");
	}

	@Override
	public void run() {
		Map<String, ClientStatus> clientStatuses = SystemStatusContainer.instance.listClientStatus();
		for (Map.Entry<String, ClientStatus> clientStatus : clientStatuses.entrySet()) {
			Cat.getProducer().logEvent("Puma.server." + clientStatus.getKey() + ".ip", clientStatus.getValue().getIp(),
					Message.SUCCESS, "name = " + clientStatus.getKey() + "&duration = " + Long.toString(period));
		}
	}

	@Override
	public void doExecute() {
		if (this.getExecutor() != null) {
			this.getExecutor().scheduleWithFixedDelay(this, initialDelay, period, unit);
		}
	}

}
