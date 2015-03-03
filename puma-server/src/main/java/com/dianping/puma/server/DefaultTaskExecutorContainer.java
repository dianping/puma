package com.dianping.puma.server;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.entity.PumaTask;
import com.dianping.puma.core.entity.SrcDBInstance;
import com.dianping.puma.core.holder.BinlogInfoHolder;
import com.dianping.puma.core.monitor.PumaTaskControllerEvent;
import com.dianping.puma.core.monitor.PumaTaskOperationEvent;
import com.dianping.puma.core.service.PumaTaskService;
import com.dianping.puma.core.service.SrcDBInstanceService;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.server.builder.TaskExecutorBuilder;
import com.dianping.puma.storage.DefaultEventStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.config.Config;
import com.dianping.puma.core.codec.JsonEventCodec;
import com.dianping.puma.core.monitor.NotifyService;
import com.dianping.puma.core.util.PumaThreadUtils;
import com.dianping.puma.datahandler.DefaultDataHandler;
import com.dianping.puma.datahandler.DefaultTableMetaInfoFetcher;
import com.dianping.puma.parser.DefaultBinlogParser;
import com.dianping.puma.parser.Parser;
import com.dianping.puma.sender.FileDumpSender;
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
	private Config pumaServerConfig;

	@PostConstruct
	public void init() {
		pumaServerName = pumaServerConfig.getName();
	}

	@Override
	public ConcurrentHashMap<String, TaskExecutor> constructServers() throws Exception {
		LOG.info("starting construct servers.........");

		List<PumaTask> pumaTasks = pumaTaskService.findByPumaServerName(pumaServerName);
		if (pumaTasks != null && !pumaTasks.isEmpty()) {
			TaskExecutor taskExecutor;
			for (PumaTask pumaTask : pumaTasks) {
				taskExecutor = construct(pumaTask);
				taskExecutorMap.put(taskExecutor.getServerName(), taskExecutor);
			}
		}

		LOG.info("ended construct servers.........");
		return taskExecutorMap;
	}

	@Override
	public TaskExecutor construct(PumaTask pumaTask) throws Exception {
		LOG.info("Construct server: {}.", pumaTask.getId());

		DefaultTaskExecutor server = new DefaultTaskExecutor();

		// Task id

		// Source database.
		String srcDBInstanceName = pumaTask.getSrcDBInstanceName();
		SrcDBInstance srcDBInstance = srcDBInstanceService.findByName(srcDBInstanceName);
		server.setServerId(srcDBInstance.getServerId());
		server.setDBHost(srcDBInstance.getHost());
		server.setPort(srcDBInstance.getPort());
		server.setDBUsername(srcDBInstance.getUsername());
		server.setDBPassword(srcDBInstance.getPassword());

		// Bin log information.
		BinlogInfo binlogInfo = pumaTask.getBinlogInfo();
		server.setDefaultBinlogFileName(binlogInfo.getBinlogFile());
		server.setDefaultBinlogPosition(binlogInfo.getBinlogPosition());
		server.setBinlogInfoHolder(binlogInfoHolder);
		server.setStatus(Status.WAITING);

		// Parser.
		Parser parser = new DefaultBinlogParser();
		parser.start();
		server.setParser(parser);

		// Table meta information.
		DefaultTableMetaInfoFetcher tableMetaInfo = new DefaultTableMetaInfoFetcher();
		tableMetaInfo.setMetaDBHost(srcDBInstance.getMetaHost());
		tableMetaInfo.setMetaDBPort(srcDBInstance.getMetaPort());
		tableMetaInfo.setMetaDBUsername(srcDBInstance.getMetaUsername());
		tableMetaInfo.setMetaDBPassword(srcDBInstance.getMetaPassword());

		// Handler.
		DefaultDataHandler dataHandler = new DefaultDataHandler();
		dataHandler.setNotifyService(notifyService);
		dataHandler.setTableMetasInfoFetcher(tableMetaInfo);
		dataHandler.start();
		server.setDataHandler(dataHandler);

		// File senders.
		FileDumpSender sender = new FileDumpSender();
		DefaultEventStorage storage = new DefaultEventStorage();
		storage.start();
		sender.setStorage(storage);
		sender.start();

		return server;
	}

	/*
	@Override
	public Server construct(ReplicationTask replicationTask) throws Exception {
		LOG.info("construct server " + replicationTask.getTaskName()
				+ ".......");
		ReplicationBasedServer server = new ReplicationBasedServer();
		server.setNotifyService(notifyService);
		server.setName(replicationTask.getTaskName());
		server.setServerId(replicationTask.getTaskId().hashCode());
		server.setDBHost(replicationTask.getDbInstanceHost().getDBHost());
		server.setPort(replicationTask.getDbInstanceHost().getPort());
		server.setDBUsername(replicationTask.getDbInstanceHost().getDBUsername());
		server.setDBPassword(replicationTask.getDbInstanceHost().getDBPassword());
		server.setDefaultBinlogFileName(replicationTask.getBinlogInfo()
				.getBinlogFile());
		server.setDefaultBinlogPosition(replicationTask.getBinlogInfo()
				.getBinlogPosition());
		server.setBinlogPositionHolder(binlogPositionHolder);
		server.setTaskStatus(ReplicationTaskStatus.Status.WAITING);
		// parser
		Parser parser = new DefaultBinlogParser();
		parser.start();
		server.setParser(parser);
		// tableMetaInfo
		DefaultTableMetaInfoFetcher tableMetaInfo = new DefaultTableMetaInfoFetcher();
		tableMetaInfo.setMetaDBHost(replicationTask.getDbInstanceMetaHost()
				.getDBHost());
		tableMetaInfo.setMetaDBPort(replicationTask.getDbInstanceMetaHost()
				.getPort());
		tableMetaInfo.setMetaDBUsername(replicationTask.getDbInstanceMetaHost()
				.getDBUsername());
		tableMetaInfo.setMetaDBPassword(replicationTask.getDbInstanceMetaHost()
				.getDBPassword());
		// handler
		DefaultDataHandler dataHandler = new DefaultDataHandler();
		dataHandler.setNotifyService(notifyService);
		dataHandler.setTableMetasInfoFetcher(tableMetaInfo);
		dataHandler.start();
		server.setDataHandler(dataHandler);
		// dispatcher
		SimpleDispatherImpl dispatcher = new SimpleDispatherImpl();
		dispatcher.setName(replicationTask.getDispatchName());
		// file senders
		List<Sender> senders = null;
		if (replicationTask.getFileSenderConfigs() != null) {
			senders = new ArrayList<Sender>();
			for (FileSenderConfig senderItem : replicationTask
					.getFileSenderConfigs()) {
				FileDumpSender sender = new FileDumpSender();
				sender.setName(senderItem.getFileSenderName());
				sender.setNotifyService(notifyService);
				// storage
				DefaultEventStorage storage = new DefaultEventStorage();
				storage.setCodec(jsonCodec);
				storage.setName(senderItem.getStorageName());
				// master storage
				LocalFileBucketIndex masterBucketIndex = new LocalFileBucketIndex();
				masterBucketIndex.setBucketFilePrefix(senderItem
						.getMasterBucketFilePrefix());
				masterBucketIndex.setMaxBucketLengthMB(senderItem
						.getMaxMasterBucketLengthMB());
				masterBucketIndex.setBaseDir(senderItem
						.getStorageMasterBaseDir());
				masterBucketIndex.start();
				storage.setMasterBucketIndex(masterBucketIndex);
				// slave storage
				LocalFileBucketIndex slaveBucketIndex = new LocalFileBucketIndex();
				slaveBucketIndex.setBucketFilePrefix(senderItem
						.getSlaveBucketFilePrefix());
				slaveBucketIndex.setMaxBucketLengthMB(senderItem
						.getMaxSlaveBucketLengthMB());
				slaveBucketIndex
						.setBaseDir(senderItem.getStorageSlaveBaseDir());
				slaveBucketIndex.start();
				storage.setSlaveBucketIndex(slaveBucketIndex);
				// archive strategy
				DefaultArchiveStrategy archiveStrategy = new DefaultArchiveStrategy();
				archiveStrategy.setServerName(replicationTask.getTaskName());
				archiveStrategy.setMaxMasterFileCount(senderItem
						.getMaxMasterFileCount());
				storage.setArchiveStrategy(archiveStrategy);
				// cleanup strategy
				DefaultCleanupStrategy cleanupStrategy = new DefaultCleanupStrategy();
				cleanupStrategy.setPreservedDay(senderItem.getPreservedDay());
				storage.setCleanupStrategy(cleanupStrategy);
				storage.setBinlogIndexBaseDir(senderItem
						.getBinlogIndexBaseDir());
				storage.start();
				sender.setStorage(storage);
				sender.start();
				senders.add(sender);
			}
		}
		dispatcher.setSenders(senders);
		dispatcher.start();
		server.setDispatcher(dispatcher);

		// Add to container.
		ReplicationTaskStatus taskStatus = new ReplicationTaskStatus(replicationTask.getTaskId());
		taskStatus.setStatus(ReplicationTaskStatus.Status.WAITING);
		replicationTaskStatusContainer.add(replicationTask.getTaskId(), taskStatus);

		return server;
	}*/

	@Override
	public void startExecutor(final TaskExecutor taskExecutor) throws Exception {
		if (taskExecutor == null) {
			throw new Exception("Null puma task executor supplied.");
		}

		if (taskExecutor.getStatus() == Status.PREPARING || taskExecutor.getStatus() == Status.RUNNING) {
			throw new Exception("Puma task is preparing or running.");
		}

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
						LOG.error("Start puma task `{}` error: {}.", taskExecutor.getTaskName(), e.getMessage());
						e.printStackTrace();
					}
				}
			}, taskExecutor.getTaskId() , false).start();
		} catch (Exception e) {
			taskExecutor.setStatus(Status.FAILED);
			throw e;
		}

		taskExecutor.setStatus(Status.RUNNING);
	}

	@Override
	public void stopExecutor(TaskExecutor taskExecutor) throws Exception {
		if (taskExecutor == null) {
			throw new Exception("Null puma task executor supplied.");
		}

		if (taskExecutor.getStatus() == Status.STOPPED || taskExecutor.getStatus() == Status.FAILED) {
			throw new Exception("Puma task is stopped or failed.");
		}

		taskExecutor.setStatus(Status.STOPPING);

		try {
			taskExecutor.stop();
		} catch (Exception e) {
			taskExecutor.setStatus(Status.FAILED);
			throw e;
		}

		taskExecutor.setStatus(Status.STOPPED);
	}

	@Override
	public void submit(TaskExecutor taskExecutor) throws Exception {
		if (taskExecutor == null) {
			throw new Exception("Null puma task executor supplied.");
		}

		String taskId = taskExecutor.getTaskId();

		if (taskExecutorMap.containsKey(taskId)) {
			throw new Exception("Duplicated puma task.");
		}

		startExecutor(taskExecutor);
		taskExecutorMap.put(taskId, taskExecutor);
	}

	@Override
	public void withdraw(TaskExecutor taskExecutor) throws Exception {
		if (taskExecutor == null) {
			throw new Exception("Null puma task executor supplied.");
		}

		String taskId = taskExecutor.getTaskId();

		if (!taskExecutorMap.containsKey(taskId)) {
			throw new Exception("Puma task not exist.");
		}

		stopExecutor(taskExecutor);
		taskExecutorMap.remove(taskId);
	}

	@Override
	public void pauseEvent(PumaTaskControllerEvent event) {
		String taskId = event.getTaskId();
		TaskExecutor taskExecutor = taskExecutorMap.get(taskId);

		try {
			stopExecutor(taskExecutor);
		} catch (Exception e) {
			LOG.error("Puma task `{}` pause event error: {}.", taskId, e.getMessage());
		}
	}

	@Override
	public void resumeEvent(PumaTaskControllerEvent event) {
		String taskId = event.getTaskId();
		TaskExecutor taskExecutor = taskExecutorMap.get(taskId);

		try {
			startExecutor(taskExecutor);
		} catch (Exception e) {
			LOG.error("Puma task `{}` resume event error: {}.", taskId, e.getMessage());
		}
	}

	@Override
	public void createEvent(PumaTaskOperationEvent event) {
		String taskId = event.getTaskId();

		try {
			PumaTask pumaTask = pumaTaskService.find(taskId);
			TaskExecutor taskExecutor = taskExecutorBuilder.build(pumaTask);
			submit(taskExecutor);
		} catch (Exception e) {
			LOG.error("Puma task `{}` create event error: {}.", taskId, e.getMessage());
		}
	}

	@Override
	public void updateEvent(PumaTaskOperationEvent event) {

	}

	@Override
	public void removeEvent(PumaTaskOperationEvent event) {
		String taskId = event.getTaskId();

		try {
			TaskExecutor taskExecutor = taskExecutorMap.get(taskId);
			withdraw(taskExecutor);
		} catch (Exception e) {
			LOG.error("Puma task `{}` remove event error: {}.", taskId, e.getMessage());
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
				LOG.error("delete Server" + task.getPumaServerName() + " failed.",
						e);
				e.printStackTrace();
			}
			ReplicationTask serverTask = replicationTaskService.findByTaskId(event
					.getTaskId());
			try {
				task = construct(serverTask);
				taskExecutorMap.put(task.getPumaServerName(), task);
				task.setTaskStatus(ReplicationTaskStatus.Status.PREPARING);
				initContext(task);
				startExecutor(task);
				task.setTaskStatus(ReplicationTaskStatus.Status.RUNNING);

			} catch (Exception e) {
				LOG.error("start Server" + task.getPumaServerName()
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
		List<Sender> senders = taskExecutorMap.get(taskName).getFileSender();
		if (senders != null && senders.size() > 0) {
			return senders.get(0).getStorage();
		}
		return null;
	}

	public Map<String, TaskExecutor> getTaskExecutorMap() {
		return Collections.unmodifiableMap(taskExecutorMap);
	}

	public String getPumaServerName() {
		return pumaServerName;
	}
}
