package com.dianping.puma.monitor;

import com.dianping.puma.core.monitor.ReplicationTaskStatusEvent;
import com.dianping.puma.server.TaskManager;
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
	private TaskManager taskManager;

	@Autowired
	private ReplicationTaskStatusContainer replicationTaskStatusContainer;

	@Scheduled(cron = "0/5 * * * * ?")
	public void report() throws SendFailedException {
		ReplicationTaskStatusEvent event = new ReplicationTaskStatusEvent();
		event.setSyncServerName("nonsense");
		event.setReplicationServerName(taskManager.getServerName());
		event.setReplicationTaskStatuses(replicationTaskStatusContainer.getAll());
		statusEventPublisher.publish(event);
	}
}
