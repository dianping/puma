package com.dianping.puma.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import com.dianping.puma.core.entity.replication.ReplicationTaskStatus;
import com.dianping.puma.monitor.ReplicationTaskStatusContainer;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.bo.PositionInfo;
import com.dianping.puma.bo.PumaContext;
import com.dianping.puma.config.InitializeServerConfig;
import com.dianping.puma.core.codec.JsonEventCodec;
import com.dianping.puma.core.monitor.NotifyService;
import com.dianping.puma.core.monitor.ReplicationTaskEvent;
import com.dianping.puma.core.monitor.ReplicationTaskStatusActionEvent;
import com.dianping.puma.core.replicate.model.config.FileSenderConfig;
import com.dianping.puma.core.replicate.model.task.ReplicationTask;
import com.dianping.puma.core.util.PumaThreadUtils;
import com.dianping.puma.datahandler.DefaultDataHandler;
import com.dianping.puma.datahandler.DefaultTableMetaInfoFetcher;
import com.dianping.puma.parser.DefaultBinlogParser;
import com.dianping.puma.parser.Parser;
import com.dianping.puma.sender.FileDumpSender;
import com.dianping.puma.sender.Sender;
import com.dianping.puma.sender.dispatcher.SimpleDispatherImpl;
import com.dianping.puma.service.ReplicationTaskService;
import com.dianping.puma.storage.DefaultArchiveStrategy;
import com.dianping.puma.storage.DefaultCleanupStrategy;
import com.dianping.puma.storage.DefaultEventStorage;
import com.dianping.puma.storage.EventStorage;
import com.dianping.puma.storage.LocalFileBucketIndex;

@Service("taskManager")
public class DefaultTaskManager implements TaskManager, InitializingBean {

	private static Logger log = Logger.getLogger(DefaultTaskManager.class);

	private ConcurrentHashMap<String, Server> serverTasks = null;

	public static DefaultTaskManager instance;
	
	@Autowired
	private ReplicationTaskService replicationTaskService;

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
		serverName = serverConfig.getServerName();
		serverTasks = new ConcurrentHashMap<String, Server>();
	}

	@Override
	public ConcurrentHashMap<String, Server> constructServers() throws Exception {
		log.info("starting construct servers.........");
		List<ReplicationTask> replicationTasks = replicationTaskService
				.find(serverName);
		if (replicationTasks != null && replicationTasks.size() > 0) {
			Server server = null;
			for (ReplicationTask replicationTask : replicationTasks) {
				server = construct(replicationTask);
				serverTasks.put(server.getServerName(), server);
			}
		}
		log.info("ended construct servers.........");
		return serverTasks;
	}

	@Override
	public Server construct(ReplicationTask replicationTask) throws Exception {
		log.info("construct server " + replicationTask.getTaskName()
				+ ".......");
		ReplicationBasedServer server = new ReplicationBasedServer();
		server.setNotifyService(notifyService);
		server.setName(replicationTask.getTaskName());
		server.setServerId(replicationTask.getTaskId().hashCode());
		server.setHost(replicationTask.getDbInstanceHost().getHost());
		server.setPort(replicationTask.getDbInstanceHost().getPort());
		server.setUser(replicationTask.getDbInstanceHost().getUsername());
		server.setPassword(replicationTask.getDbInstanceHost().getPassword());
		server.setDefaultBinlogFileName(replicationTask.getBinlogInfo()
				.getBinlogFile());
		server.setDbServerId(1);
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
		tableMetaInfo.setMetaDBUser(replicationTask.getDbInstanceMetaHost()
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
	}

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
			log.info("Server " + server.getServerName()
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
			log.info("Server " + server.getServerName() + " stopped.");
		} catch (Exception e) {
			log.error("Stop Server" + server.getServerName() + " failed.", e);
			e.printStackTrace();
		}
	}

	@Override
	public void stopServers() {
		log.info("Stopping...");
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
					log.error("Start server: " + server.getServerName()
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
					log.info("Server " + task.getServerName() + " stopped.");
				} catch (Exception e) {
					log.error(
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
	public void addEvent(ReplicationTaskEvent event) {
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
				log.info("Server " + task.getServerName()
						+ " started at binlogFile: "
						+ task.getContext().getBinlogFileName() + " position: "
						+ task.getContext().getBinlogStartPos());
			} catch (Exception e) {
				log.error("add Server" + task.getServerName() + " failed.", e);
				e.printStackTrace();
			}
		}
	}

	@Override
	public void deleteEvent(ReplicationTaskEvent event) {
		if (serverTasks != null && serverTasks.containsKey(event.getTaskName())) {
			Server task = serverTasks.get(event.getTaskName());
			try {
				task.setTaskStatus(ReplicationTaskStatus.Status.STOPPING);
				task.stop();
				task.setTaskStatus(ReplicationTaskStatus.Status.STOPPED);
				serverTasks.remove(event.getTaskName());
			} catch (Exception e) {
				log.error("delete Server" + task.getServerName() + " failed.",
						e);
				e.printStackTrace();
			}
		}
	}

	@Override
	public void updateEvent(ReplicationTaskEvent event) {
		if (serverTasks != null && serverTasks.containsKey(event.getTaskName())) {
			Server task = serverTasks.get(event.getTaskName());
			try {
				task.setTaskStatus(ReplicationTaskStatus.Status.STOPPING);
				task.stop();
				task.setTaskStatus(ReplicationTaskStatus.Status.STOPPED);
				serverTasks.remove(event.getTaskName());
			} catch (Exception e) {
				log.error("delete Server" + task.getServerName() + " failed.",
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
				log.error("start Server" + task.getServerName()
								+ " failed.", e);
				e.printStackTrace();
			}

		}
	}

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
