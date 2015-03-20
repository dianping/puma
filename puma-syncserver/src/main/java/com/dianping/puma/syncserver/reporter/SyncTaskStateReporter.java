package com.dianping.puma.syncserver.reporter;

import com.dianping.puma.core.container.SyncTaskStateContainer;
import com.dianping.puma.core.model.SyncTaskState;
import com.dianping.puma.core.monitor.SwallowEventPublisher;
import com.dianping.puma.core.monitor.SyncTaskStateEvent;
import com.dianping.puma.syncserver.conf.Config;
import com.dianping.puma.syncserver.job.container.TaskExecutionContainer;
import com.dianping.puma.syncserver.job.executor.AbstractTaskExecutor;
import com.dianping.puma.syncserver.job.executor.TaskExecutor;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service("syncTaskStateReporter")
public class SyncTaskStateReporter {

	@Autowired
	SwallowEventPublisher syncTaskStatePublisher;

	@Autowired
	SyncTaskStateContainer syncTaskStateContainer;

	@Autowired
	TaskExecutionContainer taskExecutionContainer;

	@Autowired
	Config config;

	@Scheduled(cron = "0/5 * * * * ?")
	public void report() throws SendFailedException {
		fetch();

		SyncTaskStateEvent event = new SyncTaskStateEvent();
		event.setSyncServerName(config.getSyncServerName());
		event.setSyncTaskStateMap(syncTaskStateContainer.getAll());

		syncTaskStatePublisher.publish(event);
	}

	private void fetch() {
		List<TaskExecutor> taskExecutors = taskExecutionContainer.toList();
		for(TaskExecutor taskExecutor: taskExecutors) {
			SyncTaskState state = ((AbstractTaskExecutor)taskExecutor).getState();
			state.setGmtCreate(new Date());
			syncTaskStateContainer.add(taskExecutor.getTask().getName(), state);
		}
	}
}
