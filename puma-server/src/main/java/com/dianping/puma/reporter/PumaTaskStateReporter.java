package com.dianping.puma.reporter;

import com.dianping.puma.config.Config;
import com.dianping.puma.core.container.PumaTaskStateContainer;
import com.dianping.puma.core.model.PumaTaskState;
import com.dianping.puma.core.monitor.PumaTaskStateEvent;
import com.dianping.puma.core.monitor.SwallowEventPublisher;
import com.dianping.puma.server.DefaultTaskExecutor;
import com.dianping.puma.server.TaskExecutor;
import com.dianping.puma.server.TaskExecutorContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("pumaTaskStateReporter")
public class PumaTaskStateReporter {

	private static final Logger LOG = LoggerFactory.getLogger(PumaTaskStateReporter.class);

	@Autowired
	SwallowEventPublisher pumaTaskStatePublisher;

	@Autowired
	Config pumaServerConfig;

	@Autowired
	PumaTaskStateContainer pumaTaskStateContainer;

	@Autowired
	TaskExecutorContainer taskExecutorContainer;

	@Scheduled(cron = "0/5 * * * * ?")
	public void report() {
		try {
			PumaTaskStateEvent event = new PumaTaskStateEvent();
			event.setPumaServerId(pumaServerConfig.getId());

			List<TaskExecutor> taskExecutors = taskExecutorContainer.getAll();
			for (TaskExecutor taskExecutor: taskExecutors) {
				DefaultTaskExecutor defaultTaskExecutor = (DefaultTaskExecutor) taskExecutor;
				String taskId = defaultTaskExecutor.getTaskId();
				PumaTaskState state = new PumaTaskState();
				state.setStatus(defaultTaskExecutor.getStatus());
				state.setBinlogInfo(defaultTaskExecutor.getBinlogInfo());
				state.setBinlogStat(defaultTaskExecutor.getBinlogStat());
				pumaTaskStateContainer.add(taskId, state);
			}

			event.setTaskIds(pumaTaskStateContainer.getAllTaskIds());
			event.setStates(pumaTaskStateContainer.getAll());
			pumaTaskStatePublisher.publish(event);
		} catch (Exception e) {
			LOG.error("Report puma task state error: {}.", e.getMessage());
		}
	}
}
