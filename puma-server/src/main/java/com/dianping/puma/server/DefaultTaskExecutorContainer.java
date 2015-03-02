package com.dianping.puma.server;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.entity.PumaTask;
import com.dianping.puma.core.entity.SrcDBInstanceEntity;
import com.dianping.puma.core.entity.replication.ReplicationTaskStatus;
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

import com.dianping.puma.bo.PumaContext;
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
			for (PumaTask pumaTask: pumaTasks) {
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

		ReplicationBasedTaskExecutor server = new ReplicationBasedTaskExecutor();

		// Task id

		// Source database.
		String srcDBInstanceName = pumaTask.getSrcDBInstanceName();
		SrcDBInstanceEntity srcDBInstance = srcDBInstanceService.findByName(srcDBInstanceName);
		server.setServerId(srcDBInstance.getServerId());
		server.setHost(srcDBInstance.getHost());
		server.setPort(srcDBInstance.getPort());
		server.setUsername(srcDBInstance.getUsername());
		server.setPassword(srcDBInstance.getPassword());

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
		server.setHost(replicationTask.getDbInstanceHost().getHost());
		server.setPort(replicationTask.getDbInstanceHost().getPort());
		server.setUsername(replicationTask.getDbInstanceHost().getUsername());
		server.setPassword(replicationTask.getDbInstanceHost().getPassword());
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
				.getHost());
		tableMetaInfo.setMetaDBPort(replicationTask.getDbInstanceMetaHost()
				.getPort());
		tableMetaInfo.setMetaDBUsername(replicationTask.getDbInstanceMetaHost()
				.getUsername());
		tableMetaInfo.setMetaDBPassword(replicationTask.getDbInstanceMetaHost()
				.getPassword());
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
	public void submit(TaskExecutor taskExecutor) {
		if (taskExecutor == null || taskExecutorMap.containsKey(taskExecutor.getServerName())
				|| taskExecutorMap.contains(taskExecutor)) {
			LOG.info("Submit puma task {} error.", taskExecutor.getServerName());
		}

		initContext(taskExecutor);
		startExecutor(taskExecutor);
		taskExecutor.setStatus(Status.RUNNING);
		taskExecutorMap.put(taskExecutor.getServerName(), taskExecutor);
		LOG.info("Server " + taskExecutor.getServerName()
				+ " started at binlogFile: "
				+ taskExecutor.getContext().getBinlogFileName() + " position: "
				+ taskExecutor.getContext().getBinlogStartPos());
	}

	@Override
	public void withdraw(TaskExecutor taskExecutor) {
		try {
			taskExecutor.stop();
			taskExecutor.setStatus(Status.STOPPED);
			taskExecutorMap.remove(taskExecutor.getTaskId());
		}
		catch (Exception e) {
			LOG.error("Withdraw puma task `{}` error: {}.", taskExecutor.getTaskId(), e.getMessage());
		}
	}

	@Override
	public boolean contain(String taskName) {
		if (taskExecutorMap.containsKey(taskName)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean addServer(TaskExecutor taskExecutor) {
		if (taskExecutor != null && !taskExecutorMap.containsKey(taskExecutor.getServerName())
				&& !taskExecutorMap.contains(taskExecutor)) {
			initContext(taskExecutor);
			startExecutor(taskExecutor);
			taskExecutorMap.put(taskExecutor.getServerName(), taskExecutor);
			LOG.info("Server " + taskExecutor.getServerName()
					+ " started at binlogFile: "
					+ taskExecutor.getContext().getBinlogFileName() + " position: "
					+ taskExecutor.getContext().getBinlogStartPos());
			return true;
		}
		return false;
	}

	@Override
	public void remove(String taskName) {
		taskExecutorMap.remove(taskName);
	}

	@Override
	public void initContext(TaskExecutor taskExecutor) {
		String taskName = taskExecutor.getServerName();
		BinlogInfo binlogInfo = binlogInfoHolder.getBinlogInfo(taskName);

		PumaContext context = new PumaContext();

		if (binlogInfo == null) {
			context.setBinlogFileName(taskExecutor.getDefaultBinlogFileName());
			context.setBinlogStartPos(taskExecutor.getDefaultBinlogPosition());
		} else {
			context.setBinlogFileName(binlogInfo.getBinlogFile());
			context.setBinlogStartPos(binlogInfo.getBinlogPosition());
		}

		context.setPumaServerId(taskExecutor.getServerId());
		context.setPumaServerName(taskName);
		taskExecutor.setContext(context);
	}

	@Override
	public void stopExecutor(TaskExecutor taskExecutor) {
		taskExecutor.setStatus(Status.STOPPING);

		try {
			taskExecutor.stop();
			LOG.info("Server " + taskExecutor.getServerName() + " stopped.");
		} catch (Exception e) {
			LOG.error("Stop Server" + taskExecutor.getServerName() + " failed.", e);
			e.printStackTrace();
		}

		taskExecutor.setStatus(Status.STOPPED);
	}

	@Override
	public void stopServers() {
		LOG.info("Stopping...");
		for (Map.Entry<String, TaskExecutor> item : taskExecutorMap.entrySet()) {
			stopExecutor(item.getValue());
		}
	}

	@Override
	public void startExecutor(final TaskExecutor taskExecutor) {
		taskExecutor.setStatus(Status.PREPARING);

		PumaThreadUtils.createThread(new Runnable() {
			@Override
			public void run() {
				try {
					taskExecutor.start();
				} catch (Exception e) {
					LOG.error("Start server: " + taskExecutor.getServerName()
							+ " failed.", e);
				}
			}
		}, taskExecutor.getServerName() + "_Connector", false).start();

		taskExecutor.setStatus(Status.RUNNING);
	}

	public BinlogInfoHolder getBinlogInfoHolder() {
		return binlogInfoHolder;
	}

	@Override
	public void pauseEvent(PumaTaskControllerEvent event) throws Exception {
		String taskId = event.getTaskId();
		TaskExecutor taskExecutor = taskExecutorMap.get(taskId);

		if (taskExecutor == null) {
			throw new Exception("Puma task not exist");
		}

		if (taskExecutor.getStatus() != Status.STOPPED && taskExecutor.getStatus() != Status.FAILED) {
			stopExecutor(taskExecutor);
		}
		taskExecutor.setStatus(Status.FAILED);
	}

	@Override
	public void resumeEvent(PumaTaskControllerEvent event) throws Exception {
		String taskId = event.getTaskId();
		TaskExecutor taskExecutor = taskExecutorMap.get(taskId);

		if (taskExecutor == null) {
			throw new Exception("Puma task not exist");
		}

		if (taskExecutor.getStatus() == Status.STOPPED || taskExecutor.getStatus() == Status.FAILED) {
			startExecutor(taskExecutor);
		}
		taskExecutor.setStatus(Status.FAILED);
	}

	@Override
	public void createEvent(PumaTaskOperationEvent event) throws Exception {
		String taskId = event.getTaskId();

		if (taskExecutorMap.containsKey(taskId)) {
			throw new Exception("Duplicated puma task.");
		}

		PumaTask pumaTask = pumaTaskService.find(taskId);
		TaskExecutor taskExecutor = taskExecutorBuilder.build(pumaTask);
		submit(taskExecutor);
	}

	@Override
	public void updateEvent(PumaTaskOperationEvent event) throws Exception {

	}

	@Override
	public void removeEvent(PumaTaskOperationEvent event) throws Exception {
		String taskId = event.getTaskId();
		TaskExecutor taskExecutor = taskExecutorMap.get(taskId);

		if (taskExecutor == null) {
			throw new Exception("Puma task not exist.");
		}

		withdraw(taskExecutor);
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

	public Map<String, TaskExecutor> getTaskExecutorMap(){
		return Collections.unmodifiableMap(taskExecutorMap);
	}
	
	public String getPumaServerName(){
		return pumaServerName;
	}
}
