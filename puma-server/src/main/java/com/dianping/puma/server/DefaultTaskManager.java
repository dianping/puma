package com.dianping.puma.server;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.entity.PumaTaskEntity;
import com.dianping.puma.core.entity.SrcDBInstanceEntity;
import com.dianping.puma.core.entity.replication.ReplicationTaskStatus;
import com.dianping.puma.core.monitor.PumaTaskOperationEvent;
import com.dianping.puma.core.service.PumaTaskService;
import com.dianping.puma.core.service.SrcDBInstanceService;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.monitor.ReplicationTaskStatusContainer;
import com.dianping.puma.storage.DefaultEventStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.bo.PositionInfo;
import com.dianping.puma.bo.PumaContext;
import com.dianping.puma.config.InitializeServerConfig;
import com.dianping.puma.core.codec.JsonEventCodec;
import com.dianping.puma.core.monitor.NotifyService;
import com.dianping.puma.core.monitor.ReplicationTaskStatusActionEvent;
import com.dianping.puma.core.util.PumaThreadUtils;
import com.dianping.puma.datahandler.DefaultDataHandler;
import com.dianping.puma.datahandler.DefaultTableMetaInfoFetcher;
import com.dianping.puma.parser.DefaultBinlogParser;
import com.dianping.puma.parser.Parser;
import com.dianping.puma.sender.FileDumpSender;
import com.dianping.puma.sender.Sender;
import com.dianping.puma.service.ReplicationTaskService;
import com.dianping.puma.storage.EventStorage;

@Service("taskManager")
public class DefaultTaskManager implements TaskManager, InitializingBean {

	private static Logger LOG = LoggerFactory.getLogger(DefaultTaskManager.class);

	private ConcurrentHashMap<String, Server> serverTasks = null;

	public static DefaultTaskManager instance;
	
	@Autowired
	private ReplicationTaskService replicationTaskService;

	@Autowired
	private PumaTaskService pumaTaskService;

	@Autowired
	private SrcDBInstanceService srcDBInstanceService;

	private String serverName;
	@Autowired
	private BinlogPositionHolder binlogPositionHolder;
	@Autowired
	private NotifyService notifyService;
	@Autowired
	private JsonEventCodec jsonCodec;
	@Autowired
	private InitializeServerConfig serverConfig;
	@Autowired
	private ReplicationTaskStatusContainer replicationTaskStatusContainer;

	@PostConstruct
	public void init() {
		serverName = serverConfig.getName();
		serverTasks = new ConcurrentHashMap<String, Server>();
	}

	@Override
	public ConcurrentHashMap<String, Server> constructServers() throws Exception {
		LOG.info("starting construct servers.........");

		List<PumaTaskEntity> pumaTasks = pumaTaskService.findByPumaServerName(serverName);
		if (pumaTasks != null && !pumaTasks.isEmpty()) {
			Server server;
			for (PumaTaskEntity pumaTask: pumaTasks) {
				server = construct(pumaTask);
				serverTasks.put(server.getServerName(), server);
			}
		}

		LOG.info("ended construct servers.........");
		return serverTasks;
	}

	@Override
	public Server construct(PumaTaskEntity pumaTask) throws Exception {
		LOG.info("Construct server: {}.", pumaTask.getId());

		ReplicationBasedServer server = new ReplicationBasedServer();

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
		server.setBinlogPositionHolder(binlogPositionHolder);
		server.setPumaTaskStatus(Status.WAITING);

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
	public boolean contain(String taskName) {
		if (serverTasks.containsKey(taskName)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean addServer(Server server) {
		if (server != null && !serverTasks.containsKey(server.getServerName())
				&& !serverTasks.contains(server)) {
			initContext(server);
			startServer(server);
			serverTasks.put(server.getServerName(), server);
			LOG.info("Server " + server.getServerName()
					+ " started at binlogFile: "
					+ server.getContext().getBinlogFileName() + " position: "
					+ server.getContext().getBinlogStartPos());
			return true;
		}
		return false;
	}

	@Override
	public void remove(String taskName) {
		serverTasks.remove(taskName);
	}

	@Override
	public void initContext(Server server) {
		String taskName = server.getServerName();
		PositionInfo posInfo = binlogPositionHolder.getPositionInfo(taskName,
				server.getDefaultBinlogFileName(), server
						.getDefaultBinlogPosition());
		PumaContext context = new PumaContext();

		context.setPumaServerId(server.getServerId());
		context.setPumaServerName(taskName);
		context.setBinlogFileName(posInfo.getBinlogFileName());
		context.setBinlogStartPos(posInfo.getBinlogPosition());
		server.setContext(context);
	}

	@Override
	public void stopServer(Server server) {
		try {
			server.stop();
			LOG.info("Server " + server.getServerName() + " stopped.");
		} catch (Exception e) {
			LOG.error("Stop Server" + server.getServerName() + " failed.", e);
			e.printStackTrace();
		}
	}

	@Override
	public void stopServers() {
		LOG.info("Stopping...");
		for (Map.Entry<String, Server> item : serverTasks.entrySet()) {
			stopServer(item.getValue());
		}
	}

	@Override
	public void startServer(final Server server) {
		PumaThreadUtils.createThread(new Runnable() {
			@Override
			public void run() {
				try {
					server.start();
				} catch (Exception e) {
					LOG.error("Start server: " + server.getServerName()
							+ " failed.", e);
				}
			}
		}, server.getServerName() + "_Connector", false).start();
	}

	public BinlogPositionHolder getBinlogPositionHolder() {
		return binlogPositionHolder;
	}

	@Override
	public void startEvent(ReplicationTaskStatusActionEvent event) {
		if (serverTasks != null && serverTasks.containsKey(event.getTaskId())) {
			Server task = serverTasks.get(event.getTaskId());
			task.setTaskStatus(ReplicationTaskStatus.Status.PREPARING);
			startServer(task);
			task.setTaskStatus(ReplicationTaskStatus.Status.RUNNING);
		}
	}

	@Override
	public void stopEvent(ReplicationTaskStatusActionEvent event) {
		if (serverTasks != null) {
			if (serverTasks.containsKey(event.getTaskName())) {
				Server task = serverTasks.get(event.getTaskName());
				try {
					task.setTaskStatus(ReplicationTaskStatus.Status.STOPPING);
					task.stop();
					task.setTaskStatus(ReplicationTaskStatus.Status.STOPPED);
					LOG.info("Server " + task.getServerName() + " stopped.");
				} catch (Exception e) {
					LOG.error(
							"Stop Server" + task.getServerName() + " failed.",
							e);
					task.setTaskStatus(ReplicationTaskStatus.Status.FAILED);
				}
			}
		}
	}

	@Override
	public void restartEvent(ReplicationTaskStatusActionEvent event) {
		if (serverTasks != null) {
			if (serverTasks.containsKey(event.getTaskName())) {
				Server task = serverTasks.get(event.getTaskName());
				if (task.getTaskStatus() == ReplicationTaskStatus.Status.STOPPED
						|| task.getTaskStatus() == ReplicationTaskStatus.Status.FAILED) {
					task.setTaskStatus(ReplicationTaskStatus.Status.PREPARING);
					startServer(task);
					task.setTaskStatus(ReplicationTaskStatus.Status.RUNNING);
				}
			}
		}
	}

	@Override
	public void createEvent(PumaTaskOperationEvent event) {
		String taskId = event.getTaskId();

		if (!serverTasks.containsKey(taskId)) {
			PumaTaskEntity pumaTask = pumaTaskService.find(taskId);
			Server task = null;

			try {
				task = construct(pumaTask);
				serverTasks.put(taskId, task);
				task.setPumaTaskStatus(Status.PREPARING);

				initContext(task);
				startServer(task);
				task.setPumaTaskStatus(Status.RUNNING);
			} catch (Exception e) {
				LOG.error("Create puma task `{}` error: {}.", taskId, e.getMessage());
			}
		}
	}

	@Override
	public void updateEvent(PumaTaskOperationEvent event) {

	}

	@Override
	public void removeEvent(PumaTaskOperationEvent event) {
		String taskId = event.getTaskId();

		if (serverTasks != null && serverTasks.containsKey(taskId)) {
			Server task = serverTasks.get(taskId);
			try {
				task.setPumaTaskStatus(Status.STOPPING);
				task.stop();
				task.setPumaTaskStatus(Status.STOPPED);
				serverTasks.remove(taskId);
			} catch (Exception e) {
				LOG.error("Remove puma task `{}` error: {}.", taskId, e.getMessage());
			}
		}
	}

	/*
	@Override
	public void addEvent(ReplicationTaskEvent event) {
		log.info("Adding event " + event.getTaskName() + ".");
		if (!serverTasks.containsKey(event.getTaskName())) {
			ReplicationTask serverTask = replicationTaskService.findByTaskId(event.getTaskId());
			Server task = null;
			try {
				task = construct(serverTask);
				serverTasks.put(task.getServerName(), task);
				task.setTaskStatus(ReplicationTaskStatus.Status.PREPARING);
				initContext(task);
				startServer(task);
				task.setTaskStatus(ReplicationTaskStatus.Status.RUNNING);
				LOG.info("Server " + task.getServerName()
						+ " started at binlogFile: "
						+ task.getContext().getBinlogFileName() + " position: "
						+ task.getContext().getBinlogStartPos());
			} catch (Exception e) {
				LOG.error("add Server" + task.getServerName() + " failed.", e);
				e.printStackTrace();
			}
		}
	}*/

	/*
	@Override
	public void deleteEvent(ReplicationTaskEvent event) {
		log.info("Deleting event " + event.getTaskName() + ".");
		if (serverTasks != null && serverTasks.containsKey(event.getTaskName())) {
			Server task = serverTasks.get(event.getTaskName());
			try {
				task.setTaskStatus(ReplicationTaskStatus.Status.STOPPING);
				task.stop();
				task.setTaskStatus(ReplicationTaskStatus.Status.STOPPED);
				serverTasks.remove(event.getTaskName());
			} catch (Exception e) {
				LOG.error("delete Server" + task.getServerName() + " failed.",
						e);
				e.printStackTrace();
			}
		}
	}*/

	/*
	@Override
	public void updateEvent(ReplicationTaskEvent event) {
		log.info("Updating event " + event.getTaskName() + ".");
		if (serverTasks != null && serverTasks.containsKey(event.getTaskName())) {
			Server task = serverTasks.get(event.getTaskName());
			try {
				task.setTaskStatus(ReplicationTaskStatus.Status.STOPPING);
				task.stop();
				task.setTaskStatus(ReplicationTaskStatus.Status.STOPPED);
				serverTasks.remove(event.getTaskName());
			} catch (Exception e) {
				LOG.error("delete Server" + task.getServerName() + " failed.",
						e);
				e.printStackTrace();
			}
			ReplicationTask serverTask = replicationTaskService.findByTaskId(event
					.getTaskId());
			try {
				task = construct(serverTask);
				serverTasks.put(task.getServerName(), task);
				task.setTaskStatus(ReplicationTaskStatus.Status.PREPARING);
				initContext(task);
				startServer(task);
				task.setTaskStatus(ReplicationTaskStatus.Status.RUNNING);

			} catch (Exception e) {
				LOG.error("start Server" + task.getServerName()
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
		List<Sender> senders = serverTasks.get(taskName).getFileSender();
		if (senders != null && senders.size() > 0) {
			return senders.get(0).getStorage();
		}
		return null;
	}

	public Map<String, Server> getServerTasks(){
		return Collections.unmodifiableMap(serverTasks);
	}
	
	public String getServerName(){
		return serverName;
	}
}
