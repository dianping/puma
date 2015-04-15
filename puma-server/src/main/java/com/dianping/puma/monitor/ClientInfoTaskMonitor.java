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

public class ClientInfoTaskMonitor extends AbstractTaskMonitor {

	private static final Logger LOG = LoggerFactory.getLogger(ClientInfoTaskMonitor.class);

	public static final String CLIENTINFO_INTERVAL_NAME = "puma.server.interval.clientInfo";

	public ClientInfoTaskMonitor(long initialDelay, TimeUnit unit) {
		super(initialDelay, unit);
		LOG.info("Sequence Task Monitor started.");
	}

	@Override
	public void doInit(){
		this.setInterval(getLionInterval(CLIENTINFO_INTERVAL_NAME));
		ConfigCache.getInstance().addChange(new ConfigChange() {
			@Override
			public void onChange(String key, String value) {
				if (CLIENTINFO_INTERVAL_NAME.equals(key)) {
					ClientInfoTaskMonitor.this.setInterval(Long.parseLong(value));
					if(future!=null){
						future.cancel(true);
						if(ClientInfoTaskMonitor.this.executor!=null&&!ClientInfoTaskMonitor.this.executor.isShutdown()
								&&!ClientInfoTaskMonitor.this.executor.isTerminated()){
							ClientInfoTaskMonitor.this.execute(ClientInfoTaskMonitor.this.executor);
						}
					}
				}
			}
		});
	}
	
	@Override
	public void doRun() {
		Map<String, ClientStatus> clientStatuses = SystemStatusContainer.instance.listClientStatus();
		Map<String, Long> clientSuccessSeq = SystemStatusContainer.instance.listClientSuccessSeq();
		for (Map.Entry<String, ClientStatus> clientStatus : clientStatuses.entrySet()) {
			if (clientSuccessSeq.containsKey(clientStatus.getKey())) {
				Cat.getProducer().logEvent(
						"Puma.server." + clientStatus.getKey() + ".seq",
						Long.toString(clientSuccessSeq.get(clientStatus.getKey())),
						Message.SUCCESS,
						"name = " + clientStatus.getKey() + "&target = " + clientStatus.getValue().getTarget()
								+ "&seq=" + clientSuccessSeq.get(clientStatus.getKey()).longValue() + "&duration = "
								+ Long.toString(interval));
				Cat.getProducer().logEvent(
						"Puma.server." + clientStatus.getKey() + ".binlog",
						clientStatus.getValue().getBinlogFile() + " "
								+ Long.toString(clientStatus.getValue().getBinlogPos()),
						Message.SUCCESS,
						"name = " + clientStatus.getKey() + "&target = " + clientStatus.getValue().getTarget()
								+ "&duration = " + Long.toString(interval));
			}
		}
	}

	@Override
	public Future doExecute(ScheduledExecutorService executor) {
		return executor.scheduleWithFixedDelay(this, initialDelay, interval, unit);
	}

}
