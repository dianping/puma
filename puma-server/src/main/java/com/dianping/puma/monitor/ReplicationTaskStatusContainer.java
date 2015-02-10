package com.dianping.puma.monitor;

import com.dianping.puma.common.SystemStatusContainer;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.replication.ReplicationTaskStatus;
import com.dianping.puma.core.replicate.model.task.ReplicationTask;
import com.dianping.puma.server.DefaultTaskManager;
import com.dianping.puma.server.Server;
import com.dianping.puma.service.ReplicationTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Service("replicationTaskStatusContainer")
public class ReplicationTaskStatusContainer {

	@Autowired
	private ReplicationTaskService replicationTaskService;

	private ConcurrentMap<String, ReplicationTaskStatus> taskStatusMap = new ConcurrentHashMap<String, ReplicationTaskStatus>();

	public ReplicationTaskStatus get(String taskId) {
		ReplicationTaskStatus taskStatus = taskStatusMap.get(taskId);
		if (taskStatus == null) {
			taskStatus = new ReplicationTaskStatus();
			taskStatusMap.put(taskId, taskStatus);
		}

		taskStatus.setTaskId(taskId);

		// Status.
		Map<String, Server> serverTaskMap = DefaultTaskManager.instance.getServerTasks();
		taskStatus.setStatus(DefaultTaskManager.instance.getServerTasks().get(taskId).getTaskStatus());

		// Binary log info.
		BinlogInfo binlogInfo = new BinlogInfo();
		binlogInfo.setBinlogFile(SystemStatusContainer.instance.getServerStatus(taskId).getBinlogFile());
		binlogInfo.setBinlogPosition(SystemStatusContainer.instance.getServerStatus(taskId).getBinlogPos());
		taskStatus.setBinlogInfo(binlogInfo);

		// Row or DDL changes.
		AtomicLong rowsInsert = SystemStatusContainer.instance.getServerRowInsertCounter(taskId);
		taskStatus.setRowsInsert(rowsInsert == null ? 0 : rowsInsert.longValue());
		AtomicLong rowsDelete = SystemStatusContainer.instance.getServerRowDeleteCounter(taskId);
		taskStatus.setRowsDelete(rowsDelete == null ? 0 : rowsDelete.longValue());
		AtomicLong rowsUpdate = SystemStatusContainer.instance.getServerRowUpdateCounter(taskId);
		taskStatus.setRowsUpdate(rowsUpdate == null ? 0 : rowsUpdate.longValue());
		AtomicLong ddls = SystemStatusContainer.instance.getServerDdlCounter(taskId);
		taskStatus.setDdls(ddls == null ? 0 : ddls.longValue());

		return taskStatus;
	}

	public List<ReplicationTaskStatus> getAll() {
		List<ReplicationTaskStatus> taskStatuses = new ArrayList<ReplicationTaskStatus>();

		List<ReplicationTask> replicationTasks = replicationTaskService.find(DefaultTaskManager.instance.getServerName());
		if (replicationTasks != null) {
			for (ReplicationTask replicationTask: replicationTasks) {
				taskStatuses.add(get(replicationTask.getTaskId()));
			}
		}

		return taskStatuses;
	}
}
