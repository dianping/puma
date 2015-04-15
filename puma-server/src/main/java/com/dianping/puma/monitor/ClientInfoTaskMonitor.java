package com.dianping.puma.monitor;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.puma.common.SystemStatusContainer;
import com.dianping.puma.common.SystemStatusContainer.ClientStatus;

public class ClientInfoTaskMonitor extends AbstractTaskMonitor implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(ClientInfoTaskMonitor.class);

	public ClientInfoTaskMonitor(long initialDelay, long period, TimeUnit unit) {
		super(initialDelay, period, unit);
		LOG.info("Sequence Task Monitor started.");
	}

	@Override
	public void run() {
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
								+ Long.toString(period));
				Cat.getProducer().logEvent(
						"Puma.server." + clientStatus.getKey() + ".binlog",
						clientStatus.getValue().getBinlogFile() + " "
								+ Long.toString(clientStatus.getValue().getBinlogPos()),
						Message.SUCCESS,
						"name = " + clientStatus.getKey() + "&target = " + clientStatus.getValue().getTarget()
								+ "&duration = " + Long.toString(period));
			}
		}
	}

	@Override
	public void doExecute(ScheduledExecutorService executor) {
		executor.scheduleWithFixedDelay(this, initialDelay, period, unit);
	}

}
