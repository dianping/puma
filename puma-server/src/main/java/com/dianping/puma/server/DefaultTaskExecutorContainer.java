package com.dianping.puma.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import com.dianping.puma.common.SystemStatusContainer;
import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.entity.PumaTask;
import com.dianping.puma.core.holder.BinlogInfoHolder;
import com.dianping.puma.core.model.AcceptedTables;
import com.dianping.puma.core.monitor.event.PumaTaskControllerEvent;
import com.dianping.puma.core.monitor.event.PumaTaskOperationEvent;
import com.dianping.puma.core.service.PumaTaskService;
import com.dianping.puma.core.service.SrcDBInstanceService;
import com.dianping.puma.server.builder.TaskExecutorBuilder;
import com.dianping.puma.storage.CleanupStrategy;
import com.dianping.puma.storage.DefaultCleanupStrategy;
import com.dianping.puma.storage.DefaultEventStorage;

import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.config.PumaServerConfig;
import com.dianping.puma.core.codec.JsonEventCodec;
import com.dianping.puma.core.monitor.NotifyService;
import com.dianping.puma.core.util.PumaThreadUtils;
import com.dianping.puma.datahandler.AbstractDataHandler;
import com.dianping.puma.datahandler.DataHandler;
import com.dianping.puma.datahandler.DefaultDataHandler;
import com.dianping.puma.datahandler.DefaultTableMetaInfoFetcher;
import com.dianping.puma.datahandler.TableMetasInfoFetcher;
import com.dianping.puma.sender.Sender;
import com.dianping.puma.storage.EventStorage;

@Service("taskManager")
public class DefaultTaskExecutorContainer implements TaskExecutorContainer, InitializingBean {

	private static Logger LOG = LoggerFactory.getLogger(DefaultTaskExecutorContainer.class);

	private ConcurrentHashMap<String, TaskExecutor> taskExecutorMap = new ConcurrentHashMap<String, TaskExecutor>();

	public static DefaultTaskExecutorContainer instance;

	@Autowired
	private TaskExecutorBuilder taskExecutorBuilder;

	@Autowired
	private PumaTaskService pumaTaskService;

	@Autowired
	private SrcDBInstanceService srcDBInstanceService;

	private String pumaServerName;

	@Autowired
	private BinlogInfoHolder binlogInfoHolder;

	@Autowired
	private NotifyService notifyService;

	@Autowired
	private JsonEventCodec jsonCodec;

	@Autowired
	private PumaServerConfig pumaServerConfig;

	@PostConstruct
	public void init() {
		pumaServerName = pumaServerConfig.getName();
	}

	@Override
	public TaskExecutor get(String taskName) {
		return taskExecutorMap.get(taskName);
	}

	@Override
	public List<TaskExecutor> getAll() {
		return new ArrayList<TaskExecutor>(taskExecutorMap.values());
	}

	/**
	 * Start the given task executor. Successfully started executor should
	 * eventually on `RUNNING` status, others on `FAILED` status and an
	 * exception is thrown.
	 *
	 * @param taskExecutor
	 * @throws Exception
	 */
	@Override
	public void startExecutor(final TaskExecutor taskExecutor) throws Exception {
		if (canStart(taskExecutor)) {
			taskExecutor.setStatus(Status.PREPARING);
			taskExecutor.initContext();

			try {
				PumaThreadUtils.createThread(new Runnable() {
					@Override
					public void run() {
						try {
							taskExecutor.start();
						} catch (Exception e) {
							taskExecutor.setStatus(Status.FAILED);
							LOG.error("Running puma task `{}` error: {}.", taskExecutor.getTaskName(), e.getMessage());
						}
					}
				}, taskExecutor.getTaskName(), false).start();
			} catch (Exception e) {
				taskExecutor.setStatus(Status.FAILED);
				throw e;
			}

			taskExecutor.setStatus(Status.RUNNING);
		}
	}

	/**
	 * Stop the given task executor. Successfully stopped executor should
	 * eventually on `STOPPED` status, others on `FAILED` status and an
	 * exception is thrown.
	 *
	 * @param taskExecutor
	 * @throws Exception
	 */
	@Override
	public void stopExecutor(TaskExecutor taskExecutor) throws Exception {
		if (canStop(taskExecutor)) {

			taskExecutor.setStatus(Status.STOPPING);

			try {
				taskExecutor.stop();
			} catch (Exception e) {
				taskExecutor.setStatus(Status.FAILED);
				throw e;
			}

			taskExecutor.setStatus(Status.STOPPED);
		}
	}

	/**
	 * Start the given task and submit it into the container. The start
	 * action might fail but the submit action will always be successful.
	 *
	 * @param taskExecutor
	 * @throws Exception
	 */
	@Override
	public void submit(TaskExecutor taskExecutor) throws Exception {
		try {
			startExecutor(taskExecutor);
		} catch (Exception e) {
			throw e;
		} finally {
			taskExecutorMap.put(taskExecutor.getTaskName(), taskExecutor);
		}
	}

	/**
	 * Stop the given task and withdraw it from the container. The stop
	 * action might fail but the withdraw action will always be successful.
	 *
	 * @param taskExecutor
	 * @throws Exception
	 */
	@Override
	public void withdraw(TaskExecutor taskExecutor) throws Exception {
		try {
			stopExecutor(taskExecutor);
		} catch (Exception e) {
			throw e;
		} finally {
			taskExecutorMap.remove(taskExecutor.getTaskName());
			SystemStatusContainer.instance.removeAll(taskExecutor.getTaskName());
			binlogInfoHolder.remove(taskExecutor.getTaskName());
			binlogInfoHolder.clean(taskExecutor.getTaskName());
		}
	}

	@Override
	public void pauseEvent(PumaTaskControllerEvent event) {
		String taskName = event.getTaskName();
		TaskExecutor taskExecutor = taskExecutorMap.get(taskName);

		try {
			stopExecutor(taskExecutor);
		} catch (Exception e) {
			LOG.error("Puma task `{}` pause event error: {}.", taskName, e.getMessage());
		}
	}

	@Override
	public void resumeEvent(PumaTaskControllerEvent event) {
		String taskName = event.getTaskName();
		TaskExecutor taskExecutor = taskExecutorMap.get(taskName);

		try {
			startExecutor(taskExecutor);
		} catch (Exception e) {
			LOG.error("Puma task `{}` resume event error: {}.", taskName, e.getMessage());
		}
	}

	@Override
	public void createEvent(PumaTaskOperationEvent event) {
		String taskName = event.getTaskName();

		try {
			PumaTask pumaTask = pumaTaskService.find(taskName);
			TaskExecutor taskExecutor = taskExecutorBuilder.build(pumaTask);
			submit(taskExecutor);
		} catch (Exception e) {
			LOG.error("Puma task `{}` create event error: {}.", taskName, e.getStackTrace());
		}
	}

	@Override
	public void updateEvent(PumaTaskOperationEvent event) {
		removeEvent(event);
		createEvent(event);
	}

	@Override
	public void removeEvent(PumaTaskOperationEvent event) {
		String taskName = event.getTaskName();
		TaskExecutor taskExecutor = taskExecutorMap.get(taskName);

		if (taskExecutor != null) {
			try {
				withdraw(taskExecutor);
			} catch (Exception e) {
				LOG.error("Puma task `{}` remove event error: {}.", taskName, e.getMessage());
			}
		} else {
			LOG.warn("Puma task `{}` remove event warn: {}.", taskName, "Task not found");
		}
	}

	@Override
	public void prolongEvent(PumaTaskOperationEvent event) {
		String taskName = event.getTaskName();
		TaskExecutor taskExecutor = taskExecutorMap.get(taskName);

		if (taskExecutor != null) {
			List<Sender> senders = taskExecutor.getFileSender();
			EventStorage storage = senders.get(0).getStorage();
			DefaultEventStorage defaultEventStorage = (DefaultEventStorage) storage;
			try {
				PumaTask pumaTask = pumaTaskService.find(taskName);
				CleanupStrategy cleanupStrategy = defaultEventStorage.getCleanupStrategy();
				((DefaultCleanupStrategy) cleanupStrategy).setPreservedDay(pumaTask.getPreservedDay());

			} catch (Exception e) {
				LOG.error("Puma task `{}` prolong event error: {}.", taskName, e.getMessage());
			}

		} else {
			LOG.warn("Puma task `{}` prolong event warn: {}.", taskName, "Task not found");
		}
	}
	@Override
	public void filterEvent(PumaTaskOperationEvent event){
		String taskName = event.getTaskName();
		TaskExecutor taskExecutor = taskExecutorMap.get(taskName);
		if (taskExecutor != null) {
			List<Sender> senders = taskExecutor.getFileSender();
			EventStorage storage = senders.get(0).getStorage();
			DefaultEventStorage defaultEventStorage = (DefaultEventStorage) storage;
			AbstractDataHandler handler=(AbstractDataHandler)taskExecutor.getDataHandler();
			TableMetasInfoFetcher tableMetasInfoFetcher=  handler.getTableMetasInfoFetcher();
			try {
				PumaTask pumaTask = pumaTaskService.find(taskName);
				defaultEventStorage.setAcceptedDataTables(pumaTask.getAcceptedDataInfos());
				tableMetasInfoFetcher.setAcceptedDataTables(pumaTask.getAcceptedDataInfos());
			} catch (Exception e) {
				LOG.error("Puma task `{}` prolong event error: {}.", taskName, e.getMessage());
			}
		}else{
			LOG.warn("Puma task `{}` prolong event warn: {}.", taskName, "Task not found");
		}
	}

	@Override
	public void stopServers() {
		LOG.info("Stopping...");
		for (Map.Entry<String, TaskExecutor> item : taskExecutorMap.entrySet()) {
			try {
				stopExecutor(item.getValue());
			} catch (Exception e) {
				LOG.error("Stop servers error: {}.", e.getMessage());
			}
		}
	}

	public BinlogInfoHolder getBinlogInfoHolder() {
		return binlogInfoHolder;
	}

	/*
	@Override
	public void updateEvent(ReplicationTaskEvent event) {
		if (taskExecutorMap != null && taskExecutorMap.containsKey(event.getTaskName())) {
			Server task = taskExecutorMap.get(event.getTaskName());
			try {
				task.setTaskStatus(ReplicationTaskStatus.Status.STOPPING);
				task.stop();
				task.setTaskStatus(ReplicationTaskStatus.Status.STOPPED);
				taskExecutorMap.remove(event.getTaskName());
			} catch (Exception e) {
				LOG.error("delete Server" + task.getPumaServerId() + " failed.",
						e);
				e.printStackTrace();
			}
			ReplicationTask serverTask = replicationTaskService.findByTaskId(event
					.getTaskId());
			try {
				task = construct(serverTask);
				taskExecutorMap.put(task.getPumaServerId(), task);
				task.setTaskStatus(ReplicationTaskStatus.Status.PREPARING);
				initContext(task);
				startExecutor(task);
				task.setTaskStatus(ReplicationTaskStatus.Status.RUNNING);

			} catch (Exception e) {
				LOG.error("start Server" + task.getPumaServerId()
						+ " failed.", e);
				e.printStackTrace();
			}

		}
	}*/

	@Override
	public void afterPropertiesSet() throws Exception {
		instance = this;
	}

	public EventStorage getTaskStorage(String taskName) {
		if (taskExecutorMap.containsKey(taskName)) {
			List<Sender> senders = taskExecutorMap.get(taskName).getFileSender();
			if (senders != null && senders.size() > 0) {
				return senders.get(0).getStorage();
			}
		}
		return null;
	}

	public Map<String, TaskExecutor> getTaskExecutorMap() {
		return Collections.unmodifiableMap(taskExecutorMap);
	}

	public String getPumaServerName() {
		return pumaServerName;
	}

	public boolean canStop(TaskExecutor taskExecutor) {
		return taskExecutor.getStatus() != Status.STOPPED
				&& taskExecutor.getStatus() != Status.STOPPING
				&& taskExecutor.getStatus() != Status.FAILED;
	}

	public boolean canStart(TaskExecutor taskExecutor) {
		return taskExecutor.getStatus() != Status.RUNNING
				&& taskExecutor.getStatus() != Status.PREPARING;
	}
}
