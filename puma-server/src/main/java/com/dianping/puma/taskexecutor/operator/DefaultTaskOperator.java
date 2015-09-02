package com.dianping.puma.taskexecutor.operator;

import com.dianping.puma.server.builder.TaskBuilder;
import com.dianping.puma.status.SystemStatusManager;
import com.dianping.puma.taskexecutor.TaskExecutor;
import com.dianping.puma.taskexecutor.task.DatabaseTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.dianping.puma.status.SystemStatus.*;

@Service
public class DefaultTaskOperator implements TaskOperator {

	@Autowired
	TaskBuilder taskBuilder;

	public TaskExecutor union(TaskExecutor executor, DatabaseTask databaseTask) {
		if (!canUnion(executor, databaseTask)) {
			return null;
		}

		try {
			executor.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!canUnion(executor, databaseTask)) {
			try {
				executor.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		return union0(executor, databaseTask);
	}

	public TaskExecutor union(TaskExecutor executor0, TaskExecutor executor1) {
		return null;
	}

	public TaskExecutor complement(TaskExecutor executor, String database) {
		return null;
	}

	protected boolean canUnion(TaskExecutor executor, DatabaseTask databaseTask) {
		if (databaseTask.getBeginTime() == null) {
			return true;
		}

		String taskName = executor.getTaskName();
		Server server = SystemStatusManager.getServer(taskName);

		long current = server.getBinlogInfo().getTimestamp();
		long begin = databaseTask.getBeginTime().getTime();

		if (current < begin) {
			return true;
		} else {
			return false;
		}
	}

	protected TaskExecutor union0(TaskExecutor executor, DatabaseTask databaseTask) {
		return null;
	}
}
