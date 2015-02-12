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

	private ConcurrentHashMap<String, ReplicationTaskStatus> taskStatusMap
			= new ConcurrentHashMap<String, ReplicationTaskStatus>();

	@Autowired
	ReplicationTaskService replicationTaskService;

	@PostConstruct
	public void init() {
		List<ReplicationTask> replicationTasks = replicationTaskService.findAll();
		if (replicationTasks != null) {
			for (ReplicationTask replicationTask: replicationTasks) {
				ReplicationTaskStatus replicationTaskStatus = new ReplicationTaskStatus(replicationTask.getTaskId());
				taskStatusMap.put(replicationTask.getTaskId(), replicationTaskStatus);
			}
		}
	}

	public ReplicationTaskStatus get(String taskId) {
		return taskStatusMap.get(taskId);
	}

	public void add(String taskId) {
		ReplicationTaskStatus taskStatus = new ReplicationTaskStatus(taskId);
		taskStatusMap.put(taskId, taskStatus);
	}

	public void remove(String taskId) {
		taskStatusMap.remove(taskId);
	}

	public void update(ReplicationTaskStatus taskStatus) {
		if (taskStatusMap.get(taskStatus.getTaskId()) != null) {
			taskStatusMap.put(taskStatus.getTaskId(), taskStatus);
		}
	}

	@Override
	public void onEvent(Event event) {
		System.out.println("fuck");
		if (event instanceof ReplicationTaskStatusEvent) {
			ReplicationTaskStatusEvent replicationTaskStatusEvent = (ReplicationTaskStatusEvent) event;
			List<ReplicationTaskStatus> replicationTaskStatuses = replicationTaskStatusEvent.getReplicationTaskStatuses();

			System.out.println("a");
			System.out.println(replicationTaskStatusEvent.getReplicationServerName());
			System.out.println("c");
			System.out.println(replicationTaskStatusEvent.getSyncServerName());
			if (replicationTaskStatuses != null) {
				for (ReplicationTaskStatus replicationTaskStatus: replicationTaskStatuses) {
					update(replicationTaskStatus);
				}
			}
		}
	}
}
