package com.dianping.puma.reporter;

import com.dianping.puma.config.PumaServerConfig;
import com.dianping.puma.core.model.state.TaskStateContainer;
import com.dianping.puma.core.monitor.SwallowEventPublisher;
import com.dianping.puma.server.TaskExecutorContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("pumaTaskStateReporter")
public class PumaTaskStateReporter {

	private static final Logger LOG = LoggerFactory.getLogger(PumaTaskStateReporter.class);

	@Autowired
	SwallowEventPublisher pumaTaskStatePublisher;

	@Autowired
	PumaServerConfig pumaServerConfig;

	@Autowired
	TaskStateContainer taskStateContainer;

	@Autowired
	TaskExecutorContainer taskExecutorContainer;

	/*
	@Scheduled(cron = "0/5 * * * * ?")
	public void report() {
		try {
			PumaTaskStateEvent event = new PumaTaskStateEvent();
			event.setPumaServerId(pumaServerConfig.getId());

			fetch();
			event.setStateMap(pumaTaskStateContainer.getAll());
			pumaTaskStatePublisher.publish(event);
		} catch (Exception e) {
			LOG.error("Report puma task state error: {}.", e.getMessage());
		}
	}

	private void fetch() {
		List<TaskExecutor> taskExecutors = taskExecutorContainer.getAll();
		for (TaskExecutor taskExecutor: taskExecutors) {
			DefaultTaskExecutor defaultTaskExecutor = (DefaultTaskExecutor)taskExecutor;
			PumaTaskState state = new PumaTaskState();
			state.setStatus(defaultTaskExecutor.getStatus());
			state.setBinlogInfo(defaultTaskExecutor.getBinlogInfo());
			state.setBinlogStat(defaultTaskExecutor.getBinlogStat());
			pumaTaskStateContainer.add(taskExecutor.getTaskId(), state);
		}
	}*/
}
