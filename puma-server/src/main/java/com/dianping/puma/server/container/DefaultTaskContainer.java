package com.dianping.puma.server.container;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.biz.entity.PumaTaskEntity;
import com.dianping.puma.sender.Sender;
import com.dianping.puma.server.TaskExecutor;
import com.dianping.puma.server.builder.TaskBuilder;
import com.dianping.puma.storage.EventStorage;

@Service
public class DefaultTaskContainer implements TaskContainer {

	private ConcurrentHashMap<String, TaskExecutor> taskExecutors = new ConcurrentHashMap<String, TaskExecutor>();

	public static DefaultTaskContainer instance;

	//
	// @Autowired
	// private PumaTaskStateServiceImpl pumaTaskStateService;
	//
	@Autowired
	private TaskBuilder taskBuilder;

	//
	// @Autowired
	// private PumaTaskService pumaTaskService;
	//
	// @Autowired
	// private SrcDBInstanceService srcDBInstanceService;
	//
	// private String pumaServerName;
	//
	// @Autowired
	// private BinlogInfoHolder binlogInfoHolder;
	//
	// @Autowired
	// private RawEventCodec rawCodec;
	//
	// @Autowired
	// private PumaServerConfig pumaServerConfig;
	//
	// @Autowired
	// EventCenter eventCenter;
	//
	// @Scheduled(fixedDelay = 30 * 1000)
	// public void updateState() {
	// for (TaskExecutor executor : taskExecutors.values()) {
	// try {
	// TaskStateEntity state = executor.getTaskState();
	// if (state != null) {
	// pumaTaskStateService.createOrUpdate(state);
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// //todo: log
	// }
	// }
	// }
	//
	// @PostConstruct
	// public void init() {
	// pumaServerName = pumaServerConfig.getName();
	// }
	//
	@Override
	public TaskExecutor get(String taskName) {
		return taskExecutors.get(taskName);
	}

	@Override
	public List<TaskExecutor> getAll() {
		return new ArrayList<TaskExecutor>(taskExecutors.values());
	}

	//
	// /**
	// * Start the given task executor. Successfully started executor should
	// * eventually on `RUNNING` status, others on `FAILED` status and an
	// * exception is thrown.
	// *
	// * @param taskExecutor
	// * @throws Exception
	// */
	// @Override
	// public void startExecutor(final TaskExecutor taskExecutor) throws Exception {
	// if (canStart(taskExecutor)) {
	// taskExecutor.setStatus(Status.PREPARING);
	// taskExecutor.initContext();
	//
	// try {
	// PumaThreadUtils.createThread(new Runnable() {
	// @Override
	// public void run() {
	// try {
	// taskExecutor.start();
	// } catch (Exception e) {
	// taskExecutor.setStatus(Status.FAILED);
	// LOG.error("Running puma task `{}` error: {}.", taskExecutor.getTaskName(), e.getMessage());
	// }
	// }
	// }, taskExecutor.getTaskName(), false).start();
	// } catch (Exception e) {
	// taskExecutor.setStatus(Status.FAILED);
	// throw e;
	// }
	//
	// taskExecutor.setStatus(Status.RUNNING);
	// }
	// }
	//
	// /**
	// * Stop the given task executor. Successfully stopped executor should
	// * eventually on `STOPPED` status, others on `FAILED` status and an
	// * exception is thrown.
	// *
	// * @param taskExecutor
	// * @throws Exception
	// */
	// @Override
	// public void stopExecutor(TaskExecutor taskExecutor) throws Exception {
	// if (canStop(taskExecutor)) {
	//
	// taskExecutor.setStatus(Status.STOPPING);
	//
	// try {
	// taskExecutor.stop();
	// } catch (Exception e) {
	// taskExecutor.setStatus(Status.FAILED);
	// throw e;
	// }
	//
	// taskExecutor.setStatus(Status.STOPPED);
	// }
	// }
	//
	// /**
	// * Start the given task and submit it into the container. The start action
	// * might fail but the submit action will always be successful.
	// *
	// * @param taskExecutor
	// * @throws Exception
	// */
	// @Override
	// public void submit(TaskExecutor taskExecutor) throws Exception {
	// try {
	// startExecutor(taskExecutor);
	// } catch (Exception e) {
	// throw e;
	// } finally {
	// taskExecutors.put(taskExecutor.getTaskName(), taskExecutor);
	// }
	// }
	//
	// /**
	// * Stop the given task and withdraw it from the container. The stop action
	// * might fail but the withdraw action will always be successful.
	// *
	// * @param taskExecutor
	// * @throws Exception
	// */
	// @Override
	// public void withdraw(TaskExecutor taskExecutor) throws Exception {
	// try {
	// stopExecutor(taskExecutor);
	// } catch (Exception e) {
	// throw e;
	// } finally {
	// taskExecutors.remove(taskExecutor.getTaskName());
	// SystemStatusContainer.instance.removeAll(taskExecutor.getTaskName());
	// binlogInfoHolder.remove(taskExecutor.getTaskName());
	// binlogInfoHolder.clean(taskExecutor.getTaskName());
	// }
	// }

	// @Override
	// public void prolongEvent(PumaTaskOperationEvent event) {
	// String taskName = event.getTaskName();
	// TaskExecutor taskExecutor = taskExecutors.get(taskName);
	//
	// if (taskExecutor != null) {
	// List<Sender> senders = taskExecutor.getFileSender();
	// EventStorage storage = senders.get(0).getStorage();
	// DefaultEventStorage defaultEventStorage = (DefaultEventStorage) storage;
	// try {
	// PumaTask pumaTask = pumaTaskService.find(taskName);
	// CleanupStrategy cleanupStrategy = defaultEventStorage.getCleanupStrategy();
	// ((DefaultCleanupStrategy) cleanupStrategy).setPreservedDay(pumaTask.getPreservedDay());
	//
	// } catch (Exception e) {
	// LOG.error("Puma task `{}` prolong event error: {}.", taskName, e.getMessage());
	// }
	//
	// } else {
	// LOG.warn("Puma task `{}` prolong event warn: {}.", taskName, "Task not found");
	// }
	// }
	//
	// @Override
	// public void filterEvent(PumaTaskOperationEvent event) {
	// String taskName = event.getTaskName();
	// TaskExecutor taskExecutor = taskExecutors.get(taskName);
	// if (taskExecutor != null) {
	// AbstractDataHandler handler = (AbstractDataHandler) taskExecutor.getDataHandler();
	// TableMetaInfoFetcher tableMetasInfoFetcher = handler.getTableMetasInfoFetcher();
	// try {
	// publishAcceptedTableChangedEvent(event.getTaskName(), event.getPumaTask().getTableSet());
	// tableMetasInfoFetcher.refreshTableMeta(null, true);
	// } catch (Exception e) {
	// LOG.error("Puma task `{}` prolong event error: {}.", taskName, e.getMessage());
	// }
	// } else {
	// LOG.warn("Puma task `{}` prolong event warn: {}.", taskName, "Task not found");
	// }
	// }

	// @Override
	// @PreDestroy
	// public void stopServers() {
	// LOG.info("Stopping...");
	// for (Map.Entry<String, TaskExecutor> item : taskExecutors.entrySet()) {
	// try {
	// stopExecutor(item.getValue());
	// } catch (Exception e) {
	// LOG.error("Stop servers error: {}.", e.getMessage());
	// }
	// }
	// }
	//
	// public BinlogInfoHolder getBinlogInfoHolder() {
	// return binlogInfoHolder;
	// }
	//
	// @Override
	// public void afterPropertiesSet() throws Exception {
	// instance = this;
	// }
	//

	@Override
	public EventStorage getTaskStorage(String taskName) {
		if (taskExecutors.containsKey(taskName)) {
			List<Sender> senders = taskExecutors.get(taskName).getFileSender();
			if (senders != null && senders.size() > 0) {
				return senders.get(0).getStorage();
			}
		}
		return null;
	}

	//
	// public Map<String, TaskExecutor> getTaskExecutors() {
	// return Collections.unmodifiableMap(taskExecutors);
	// }
	//
	// public String getPumaServerName() {
	// return pumaServerName;
	// }
	//
	// public boolean canStop(TaskExecutor taskExecutor) {
	// return taskExecutor.getStatus() != Status.STOPPED && taskExecutor.getStatus() != Status.STOPPING
	// && taskExecutor.getStatus() != Status.FAILED;
	// }
	//
	// public boolean canStart(TaskExecutor taskExecutor) {
	// return taskExecutor.getStatus() != Status.RUNNING && taskExecutor.getStatus() != Status.PREPARING;
	// }
	//
	// public void publishAcceptedTableChangedEvent(String name, TableSet tableSet) {
	// AcceptedTableChangedEvent acceptedTableChangedEvent = new AcceptedTableChangedEvent();
	// acceptedTableChangedEvent.setName(name);
	// acceptedTableChangedEvent.setTableSet(tableSet);
	//
	// eventCenter.post(acceptedTableChangedEvent);
	// }

	@Override
	public void create(String taskName, PumaTaskEntity task) {
		try {
			TaskExecutor taskExecutor = taskBuilder.build(task);

			if (taskExecutors.putIfAbsent(taskName, taskExecutor) != null) {
				throw new RuntimeException("create puma task failure, duplicate exists.");
			}

			start(taskName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update(String taskName, PumaTaskEntity oriTask, PumaTaskEntity task) {

	}

	@Override
	public void delete(String taskName, PumaTaskEntity task) {
		if (taskExecutors.remove(taskName) == null) {
			throw new RuntimeException("delete puma task failure, not exists.");
		}

		stop(taskName);
	}

	@Override
	public void start(String taskName) {
		TaskExecutor taskExecutor = taskExecutors.get(taskName);
		if (taskExecutor == null) {
			throw new RuntimeException("start puma task failure, not exists.");
		}

		try {
			taskExecutor.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void stop(String taskName) {
		TaskExecutor taskExecutor = taskExecutors.get(taskName);
		if (taskExecutor == null) {
			throw new RuntimeException("stop puma task failure, not exists.");
		}

		try {
			taskExecutor.stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}