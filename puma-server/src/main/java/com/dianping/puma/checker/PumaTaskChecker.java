package com.dianping.puma.checker;

import com.dianping.puma.config.Config;
import com.dianping.puma.core.constant.Controller;
import com.dianping.puma.core.constant.Operation;
import com.dianping.puma.core.entity.PumaTask;
import com.dianping.puma.core.model.PumaTaskController;
import com.dianping.puma.core.model.PumaTaskOperation;
import com.dianping.puma.core.monitor.Event;
import com.dianping.puma.core.monitor.EventListener;
import com.dianping.puma.core.monitor.PumaTaskControllerEvent;
import com.dianping.puma.core.monitor.PumaTaskOperationEvent;
import com.dianping.puma.core.service.PumaTaskService;
import com.dianping.puma.server.TaskExecutor;
import com.dianping.puma.server.TaskExecutorContainer;
import com.dianping.puma.server.builder.TaskExecutorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service("pumaTaskChecker")
public class PumaTaskChecker implements EventListener {

	public static final Logger LOG = LoggerFactory.getLogger(PumaTaskChecker.class);

	@Autowired
	private Config pumaServerConfig;

	@Autowired
	private PumaTaskService pumaTaskService;

	@Autowired
	private TaskExecutorBuilder taskExecutorBuilder;

	@Autowired
	private TaskExecutorContainer taskExecutorContainer;

	@PostConstruct
	public void init() {
		String pumaServerName = pumaServerConfig.getName();
		List<PumaTask> pumaTasks = pumaTaskService.findByPumaServerName(pumaServerName);

		for (PumaTask pumaTask: pumaTasks) {
			try {
				TaskExecutor taskExecutor = taskExecutorBuilder.build(pumaTask);
				taskExecutorContainer.submit(taskExecutor);
			} catch (Exception e) {
				LOG.error("Initialize puma task `{}` error: {}.", pumaTask.getName(), e.getMessage());
			}
		}
	}

	@Override
	public void onEvent(Event event) {
		LOG.info("Receive puma task event!");

		if (event instanceof PumaTaskOperationEvent) {
			LOG.info("Receive puma task operation event.");

			try {
				PumaTaskOperationEvent pumaTaskOperationEvent = (PumaTaskOperationEvent) event;
				PumaTaskOperation pumaTaskOperation = pumaTaskOperationEvent.getOperation();
				Operation operation = pumaTaskOperation.getOperation();

				switch (operation) {
				case CREATE:
					LOG.info("Receive puma task operation event: CREATE.");
					taskExecutorContainer.createEvent(pumaTaskOperationEvent);
					break;
				case UPDATE:
					LOG.info("Receive puma task operation event: UPDATE.");
					taskExecutorContainer.updateEvent(pumaTaskOperationEvent);
					break;
				case REMOVE:
					LOG.info("Receive puma task operation event: REMOVE.");
					taskExecutorContainer.removeEvent(pumaTaskOperationEvent);
					break;
				default:
					LOG.error("Receive illegal puma task operation event: {}.", operation);
				}
			} catch (Exception e) {
				LOG.error("Execute puma task operation error: {}.", e.getMessage());
			}
		} else if (event instanceof PumaTaskControllerEvent) {
			LOG.info("Receive puma task controller event.");

			try {
				PumaTaskControllerEvent pumaTaskControllerEvent = (PumaTaskControllerEvent) event;
				PumaTaskController pumaTaskController = pumaTaskControllerEvent.getController();
				Controller controller = pumaTaskController.getController();

				switch (controller) {
				case PAUSE:
					LOG.info("Receive puma task controller event: PAUSE.");
					taskExecutorContainer.pauseEvent(pumaTaskControllerEvent);
					break;
				case RESUME:
					LOG.info("Receive puma task controller event: RESUME.");
					taskExecutorContainer.resumeEvent(pumaTaskControllerEvent);
					break;
				default:
					LOG.error("Receive illegal puma task controller event: {}.", controller);
				}
			}
			catch (Exception e) {
				LOG.error("Execute puma task controller event error: {}.", e.getMessage());
			}
		} else {
			LOG.error("Receive illegal puma task event: {}.", event);
		}
	}
}
