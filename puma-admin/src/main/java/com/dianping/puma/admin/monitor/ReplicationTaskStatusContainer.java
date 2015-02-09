package com.dianping.puma.admin.monitor;

import com.dianping.puma.admin.service.ReplicationTaskService;
import com.dianping.puma.core.model.replication.ReplicationTaskStatus;
import com.dianping.puma.core.monitor.Event;
import com.dianping.puma.core.monitor.EventListener;
import com.dianping.puma.core.monitor.ReplicationTaskStatusEvent;
import com.dianping.puma.core.replicate.model.task.ReplicationTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service("replicationTaskStatusContainer")
public class ReplicationTaskStatusContainer implements EventListener {

	private ConcurrentHashMap<String, ReplicationTaskStatus> statusMap
			= new ConcurrentHashMap<String, ReplicationTaskStatus>();

	@Autowired
	ReplicationTaskService replicationTaskService;

	@PostConstruct
	public void init() {
		List<ReplicationTask> replicationTasks = replicationTaskService.findAll();
		if (replicationTasks != null) {
			for (ReplicationTask replicationTask: replicationTasks) {
				ReplicationTaskStatus replicationTaskStatus = new ReplicationTaskStatus();
				replicationTaskStatus.setTaskId(replicationTask.getTaskId());
				replicationTaskStatus.setStatus(ReplicationTaskStatus.Status.WAITING);
				statusMap.put(replicationTask.getTaskId(), replicationTaskStatus);
			}
		}
	}

	public void updateStatus(ReplicationTaskStatus replicationTaskStatus) {
		if (statusMap.get(replicationTaskStatus.getTaskId()) != null) {
			replicationTaskStatus.setGmtCreate(new Date());
			statusMap.put(replicationTaskStatus.getTaskId(), replicationTaskStatus);
		}
	}

	public ReplicationTaskStatus getStatus(String taskId) {
		return statusMap.get(taskId);
	}

	@Override
	public void onEvent(Event event) {
		if (event instanceof ReplicationTaskStatusEvent) {
			ReplicationTaskStatusEvent replicationTaskStatusEvent = (ReplicationTaskStatusEvent) event;
			List<ReplicationTaskStatus> replicationTaskStatuses = replicationTaskStatusEvent.getReplicationTaskStatuses();

			if (replicationTaskStatuses != null) {
				for (ReplicationTaskStatus replicationTaskStatus: replicationTaskStatuses) {
					updateStatus(replicationTaskStatus);
				}
			}
		}
	}
}
