package com.dianping.puma.syncserver.remote.reporter;

import com.dianping.puma.core.model.state.SyncTaskState;
import com.dianping.puma.core.model.state.TaskState;
import com.dianping.puma.core.model.state.TaskStateContainer;
import com.dianping.puma.core.monitor.SwallowEventPublisher;
import com.dianping.puma.core.monitor.event.SyncTaskStateEvent;
import com.dianping.puma.syncserver.conf.Config;
import com.dianping.puma.syncserver.job.container.TaskExecutionContainer;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("syncTaskStateReporter")
public class SyncTaskStateReporter {

	@Autowired
	SwallowEventPublisher syncTaskStatePublisher;

	@Autowired
	TaskStateContainer syncTaskStateContainer;

	@Autowired
	TaskExecutionContainer taskExecutionContainer;

	@Autowired
	Config config;

	@Scheduled(cron = "0/5 * * * * ?")
	public void report() throws SendFailedException {
		SyncTaskStateEvent event = new SyncTaskStateEvent();
		event.setServerName(config.getSyncServerName());

		Map<String, SyncTaskState> syncTaskStates = new HashMap<String, SyncTaskState>();
		Map<String, TaskState> taskStates = syncTaskStateContainer.getAll();
		for (Map.Entry<String, TaskState> entry: taskStates.entrySet()) {
			String taskName = entry.getKey();
			TaskState taskState = entry.getValue();

			if (taskState instanceof SyncTaskState) {
				syncTaskStates.put(taskName, (SyncTaskState) taskState);
			}
		}

		event.setTaskStates(syncTaskStates);
	}
}
