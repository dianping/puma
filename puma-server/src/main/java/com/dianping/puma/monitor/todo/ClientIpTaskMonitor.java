package com.dianping.puma.monitor.todo;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.puma.common.SystemStatusContainer;
import com.dianping.puma.common.SystemStatusContainer.ClientStatus;
import com.dianping.puma.monitor.MonitorScheduledExecutor;

@Service("clientIpTaskMonitor")
public class ClientIpTaskMonitor extends AbstractTaskMonitor implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(ClientIpTaskMonitor.class);

	public static final String CLIENTIP_INTERVAL_NAME = "puma.server.interval.ip";
	
//	@Autowired
//	private MonitorScheduledExecutor monitorScheduledExecutor;
	
	public ClientIpTaskMonitor() {
		super(0, TimeUnit.MILLISECONDS);
		LOG.info("ClientIp Task Monitor started.");
	}

	@Override
	public void doInit() {
		this.setInterval(getLionInterval(CLIENTIP_INTERVAL_NAME));
		ConfigCache.getInstance().addChange(new ConfigChange() {
			@Override
			public void onChange(String key, String value) {
				if (CLIENTIP_INTERVAL_NAME.equals(key)) {
					ClientIpTaskMonitor.this.setInterval(Long.parseLong(value));
					if (future != null && !future.isCancelled() && !future.isDone()) {
						future.cancel(true);
						if (getMonitorScheduledExecutor().isScheduledValid()) {
							ClientIpTaskMonitor.this.execute();
						}
					}
				}
			}
		});
	}

	@Override
	public void doRun() {
		Map<String, ClientStatus> clientStatuses = SystemStatusContainer.instance.listClientStatus();
		for (Map.Entry<String, ClientStatus> clientStatus : clientStatuses.entrySet()) {
			Cat.getProducer().logEvent("Puma.server." + clientStatus.getKey() + ".ip", clientStatus.getValue().getIp(),
					Message.SUCCESS, "name = " + clientStatus.getKey() + "&duration = " + Long.toString(getInterval()));
		}
	}
	
	@PostConstruct
	@Override
	public void doExecute() {
		future = getMonitorScheduledExecutor().getExecutorService().scheduleWithFixedDelay(this, getInitialDelay(),
				getInterval(), getUnit());
	}

//	public MonitorScheduledExecutor getMonitorScheduledExecutor() {
//		return monitorScheduledExecutor;
//	}
//
//	public void setMonitorScheduledExecutor(MonitorScheduledExecutor monitorScheduledExecutor) {
//		this.monitorScheduledExecutor = monitorScheduledExecutor;
//	}
	
}
