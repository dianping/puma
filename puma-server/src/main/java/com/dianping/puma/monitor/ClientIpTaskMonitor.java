package com.dianping.puma.monitor;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.puma.common.SystemStatusContainer;
import com.dianping.puma.common.SystemStatusContainer.ClientStatus;

public class ClientIpTaskMonitor extends AbstractTaskMonitor implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(ClientIpTaskMonitor.class);

	public static final String CLIENTIP_INTERVAL_NAME = "puma.server.interval.ip";
	
	public ClientIpTaskMonitor(long initialDelay, TimeUnit unit) {
		super(initialDelay, unit);
		LOG.info("ClientIp Task Monitor started.");
	}
	
	@Override
	public void doInit(){
		this.setInterval(getLionInterval(CLIENTIP_INTERVAL_NAME));
		ConfigCache.getInstance().addChange(new ConfigChange() {
			@Override
			public void onChange(String key, String value) {
				if (CLIENTIP_INTERVAL_NAME.equals(key)) {
					ClientIpTaskMonitor.this.setInterval(Long.parseLong(value));
					if(future!=null){
						future.cancel(true);
						if(ClientIpTaskMonitor.this.executor!=null&&!ClientIpTaskMonitor.this.executor.isShutdown()
								&&!ClientIpTaskMonitor.this.executor.isTerminated()){
							ClientIpTaskMonitor.this.execute(ClientIpTaskMonitor.this.executor);
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
					Message.SUCCESS, "name = " + clientStatus.getKey() + "&duration = " + Long.toString(interval));
		}
	}

	@Override
	public Future doExecute(ScheduledExecutorService executor) {
		return executor.scheduleWithFixedDelay(this, initialDelay, interval, unit);
	}

}
