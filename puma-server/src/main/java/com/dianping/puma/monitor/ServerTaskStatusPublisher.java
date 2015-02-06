package com.dianping.puma.monitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.dianping.puma.config.InitializeServerConfig;
import com.dianping.puma.core.monitor.ReplicationTaskReportEvent;
import com.dianping.puma.core.monitor.SwallowEventPublisher;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;

@Service
public class ServerTaskStatusPublisher {

	@Autowired
	private SwallowEventPublisher statusEventPublisher;

	@Autowired
	private InitializeServerConfig serverConfig;
	
	@Scheduled(cron = "0/5 * * * * ?")
	public void report() throws SendFailedException {
		ReplicationTaskReportEvent event = TaskExecutorStatusContainer.instance
				.getReportEvent();
		event.setReplicationServerName(serverConfig.getServerName());
		if (event.getStatusList() != null && event.getStatusList().size() > 0) {
			statusEventPublisher.publish(event);
		}
	}
}
