package com.dianping.puma.syncserver.remote.reporter.helper;

import com.dianping.puma.core.model.state.TaskState;
import com.dianping.puma.core.model.state.TaskStateContainer;
import com.dianping.puma.syncserver.job.container.TaskExecutionContainer;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TaskStateCollector {

	@Autowired
	TaskStateContainer syncTaskStateContainer;

	@Autowired
	TaskExecutionContainer taskExecutionContainer;

	@Scheduled(cron = "0/5 * * * * ?")
	public void collect() {
		syncTaskStateContainer.removeAll();

		for (TaskExecutor taskExecutor: taskExecutionContainer.getAll()) {
			TaskState taskState = taskExecutor.getTaskState();
			taskState.setGmtUpdate(new Date());
			syncTaskStateContainer.add(taskExecutor.getTask().getName(), taskState);
		}
	}
}
