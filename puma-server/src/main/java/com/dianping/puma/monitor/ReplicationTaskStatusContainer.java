package com.dianping.puma.monitor;

import com.dianping.puma.common.SystemStatusContainer;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.replication.ReplicationTaskStatus;
import com.dianping.puma.core.replicate.model.task.ReplicationTask;
import com.dianping.puma.server.DefaultTaskManager;
import com.dianping.puma.service.ReplicationTaskService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ReplicationTaskStatusContainer {

	@Autowired
	private SystemStatusContainer systemStatusContainer;

	@Autowired
	private ReplicationTaskService replicationTaskService;

	private ConcurrentMap<String, ReplicationTaskStatus> taskStatusMap = new ConcurrentHashMap<String, ReplicationTaskStatus>();

	public ReplicationTaskStatus get(String taskId) {
		ReplicationTaskStatus taskStatus = taskStatusMap.get(taskId);
		if (taskStatus == null) {
			taskStatus = new ReplicationTaskStatus();
			taskStatusMap.put(taskId, taskStatus);
		}

		// Status.
		taskStatus.setStatus(DefaultTaskManager.instance.getServerTasks().get(taskId).getTaskStatus());

		// Binary log info.
		BinlogInfo binlogInfo = new BinlogInfo();
		binlogInfo.setBinlogFile(systemStatusContainer.getServerStatus(taskId).getBinlogFile());
		binlogInfo.setBinlogPosition(systemStatusContainer.getServerStatus(taskId).getBinlogPos());

		// Row or DDL changes.
		taskStatus.setRowsInsert(systemStatusContainer.getServerRowInsertCounter(taskId).longValue());
		taskStatus.setRowsDelete(systemStatusContainer.getServerRowDeleteCounter(taskId).longValue());
		taskStatus.setRowsUpdate(systemStatusContainer.getServerRowUpdateCounter(taskId).longValue());
		taskStatus.setDdls(systemStatusContainer.getServerDdlCounter(taskId).longValue());

		return taskStatus;
	}

	public List<ReplicationTaskStatus> getAll() {
		List<ReplicationTaskStatus> taskStatuses = new ArrayList<ReplicationTaskStatus>();

		List<ReplicationTask> replicationTasks = replicationTaskService.findAll();
		if (replicationTasks != null) {
			for (ReplicationTask replicationTask: replicationTasks) {
				taskStatuses.add(get(replicationTask.getTaskId()));
			}
		}

		return taskStatuses;
	}
}
