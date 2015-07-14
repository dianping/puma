//package com.dianping.puma.remote.receiver;
//
//import com.dianping.puma.config.PumaServerConfig;
//import com.dianping.puma.core.constant.ActionOperation;
//import com.dianping.puma.biz.entity.PumaTask;
//import com.dianping.puma.core.model.event.EventCenter;
//import com.dianping.puma.biz.event.entity.Event;
//import com.dianping.puma.biz.event.EventListener;
//import com.dianping.puma.biz.event.entity.PumaTaskOperationEvent;
//import com.dianping.puma.biz.service.PumaTaskService;
//import com.dianping.puma.server.TaskExecutor;
//import com.dianping.puma.server.container.TaskExecutorContainer;
//import com.dianping.puma.server.builder.TaskExecutorBuilder;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.PostConstruct;
//import java.util.List;
//
//@Service("pumaTaskOperationChecker")
//public class PumaTaskOperationChecker implements EventListener {
//
//	public static final Logger LOG = LoggerFactory.getLogger(PumaTaskOperationChecker.class);
//
//	@Autowired
//	private PumaServerConfig pumaServerConfig;
//
//	@Autowired
//	private PumaTaskService pumaTaskService;
//
//	@Autowired
//	private TaskExecutorBuilder taskExecutorBuilder;
//
//	@Autowired
//	private TaskExecutorContainer taskExecutorContainer;
//
//	@Autowired
//	EventCenter eventCenter;
//
//	@PostConstruct
//	public void init() {
//		String pumaServerName = pumaServerConfig.getName();
//
//		// Throws puma task service exceptions.
//		List<PumaTask> pumaTasks = pumaTaskService.findByPumaServerNames(pumaServerName);
//		List<PumaTask> pumaTaskOlds = pumaTaskService.findByPumaServerName(pumaServerName);
//
//		// Swallows puma task executors exceptions.
//		for (PumaTask pumaTask : pumaTasks) {
//			try {
//				TaskExecutor taskExecutor = taskExecutorBuilder.build(pumaTask);
//				taskExecutorContainer.publishAcceptedTableChangedEvent(pumaTask.getName(), pumaTask.getTableSet());
//				taskExecutorContainer.submit(taskExecutor);
//			} catch (Exception e) {
//				LOG.error("Initialize puma task `{}` error: {}.", pumaTask.getName(), e.getStackTrace());
//			}
//		}
//		for (PumaTask pumaTask : pumaTaskOlds) {
//			try {
//				TaskExecutor taskExecutor = taskExecutorBuilder.build(pumaTask);
//				taskExecutorContainer.publishAcceptedTableChangedEvent(pumaTask.getName(), pumaTask.getTableSet());
//				taskExecutorContainer.submit(taskExecutor);
//			} catch (Exception e) {
//				LOG.error("Initialize puma task `{}` error: {}.", pumaTask.getName(), e.getStackTrace());
//			}
//		}
//	}
//
//	@Override
//	public void onEvent(Event event) {
//		LOG.info("Receive puma task event!");
//
//		if (event instanceof PumaTaskOperationEvent) {
//			LOG.info("Receive puma task operation event.");
//
//			PumaTaskOperationEvent pumaTaskOperationEvent = (PumaTaskOperationEvent) event;
//			ActionOperation operation = pumaTaskOperationEvent.getOperation();
//
//			switch (operation) {
//			case CREATE:
//				LOG.info("Receive puma task operation event: CREATE.");
//				taskExecutorContainer.createEvent(pumaTaskOperationEvent);
//				break;
//			case UPDATE:
//				LOG.info("Receive puma task operation event: UPDATE.");
//				updateEvent(pumaTaskOperationEvent);
//				// taskExecutorContainer.updateEvent(pumaTaskOperationEvent);
//				break;
//			case REMOVE:
//				LOG.info("Receive puma task operation event: REMOVE.");
//				taskExecutorContainer.removeEvent(pumaTaskOperationEvent);
//				break;
//			case FILTER:
//				LOG.info("Receive puma task operation event: FILTER or CHANGE.");
//				taskExecutorContainer.prolongEvent(pumaTaskOperationEvent);
//				taskExecutorContainer.filterEvent(pumaTaskOperationEvent);
//				break;
//			case CHANGE:
//				LOG.info("Receive puma task operation event: PROLONG or CHANGE.");
//				taskExecutorContainer.prolongEvent(pumaTaskOperationEvent);
//				break;
//			default:
//				LOG.error("Receive illegal puma task operation event `{}`.", operation);
//			}
//		} else {
//			LOG.error("Receive illegal puma task event `{}`.", event);
//		}
//	}
//
//	private void updateEvent(PumaTaskOperationEvent pumaTaskOperationEvent) {
//		PumaTask oriPumaTask = pumaTaskOperationEvent.getOriPumaTask();
//		PumaTask pumaTask = pumaTaskOperationEvent.getPumaTask();
//		if (oriPumaTask != null && pumaTask != null && !oriPumaTask.equals(pumaTask)) {
//			if (!oriPumaTask.getTableSet().equals(pumaTask.getTableSet())) {
//				LOG.info("`{}` Task Accepted Table CHANGE.", pumaTaskOperationEvent.getTaskName());
//				taskExecutorContainer.filterEvent(pumaTaskOperationEvent);
//			} else if (oriPumaTask.getPreservedDay() != pumaTask.getPreservedDay()) {
//				LOG.info("`{}` Task PreservedDay CHANGE.", pumaTaskOperationEvent.getTaskName());
//				taskExecutorContainer.prolongEvent(pumaTaskOperationEvent);
//			} else if (!oriPumaTask.getBinlogInfo().equals(pumaTask.getBinlogInfo())) {
//				LOG.info("`{}` Task Need Restart.", pumaTaskOperationEvent.getTaskName());
//				taskExecutorContainer.updateEvent(pumaTaskOperationEvent);
//			}
//		}
//		return;
//	}
//}
