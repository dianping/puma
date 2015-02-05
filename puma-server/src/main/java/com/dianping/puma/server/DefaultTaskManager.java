package com.dianping.puma.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.bo.PositionInfo;
import com.dianping.puma.bo.PumaContext;
import com.dianping.puma.config.InitializeServerConfig;
import com.dianping.puma.core.codec.JsonEventCodec;
import com.dianping.puma.core.monitor.NotifyService;
import com.dianping.puma.core.monitor.ReplicationTaskEvent;
import com.dianping.puma.core.monitor.ReplicationTaskStatusEvent;
import com.dianping.puma.core.replicate.model.config.FileSenderConfig;
import com.dianping.puma.core.replicate.model.task.ReplicationTask;
import com.dianping.puma.core.replicate.model.task.StatusActionType;
import com.dianping.puma.core.replicate.model.task.StatusExecutorType;
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
public class DefaultTaskManager implements TaskManager {

	private static Logger log = Logger.getLogger(DefaultTaskManager.class);

	private static ConcurrentHashMap<Long, Server> serverTasks = null;
	
	private static ConcurrentHashMap<String,EventStorage> taskStorage = null;
	
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

	@PostConstruct
	public void init() {
		serverName = serverConfig.getServerName();
		taskStorage = new ConcurrentHashMap<String,EventStorage>();
		serverTasks = new ConcurrentHashMap<Long, Server>();
	}
	

	@Override
	public ConcurrentHashMap<Long, Server> constructServers() throws Exception {
		log.info("starting construct servers.........");
		List<ReplicationTask> replicationTasks = replicationTaskService
				.find(serverName);
		if (replicationTasks != null && replicationTasks.size() > 0) {			
			Server server = null;
			for (ReplicationTask replicationTask : replicationTasks) {
				server = construct(replicationTask);
				serverTasks.put(server.getServerId(), server);
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
		server.setServerId(replicationTask.getTaskId());
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
		server.setStatusExecutorType(StatusExecutorType.WAITING);
		// parser
		Parser parser = new DefaultBinlogParser();
		parser.start();
		server.setParser(parser);
		// tableMetaInfo
		DefaultTableMetaInfoFetcher tableMetaInfo = new DefaultTableMetaInfoFetcher();
		tableMetaInfo.setMetaDBHost(replicationTask.getDbInstanceMetaHost().getHost());
		tableMetaInfo.setMetaDBPort(replicationTask.getDbInstanceMetaHost().getPort());
		tableMetaInfo.setMetaDBUser(replicationTask.getDbInstanceMetaHost().getUsername());
		tableMetaInfo.setMetaDBPassword(replicationTask.getDbInstanceMetaHost().getPassword());
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
				taskStorage.put(storage.getName(), storage);
			}
		}
		dispatcher.setSenders(senders);
		dispatcher.start();
		server.setDispatcher(dispatcher);
		return server;
	}

	@Override
	public ConcurrentHashMap<Long, Server> getServers() {
		return serverTasks;
	}

	@Override
	public boolean contain(Long taskId) {
		if (serverTasks.containsKey(taskId)) {
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
			serverTasks.put(server.getServerId(), server);
			log.info("Server " + server.getServerName()
					+ " started at binlogFile: "
					+ server.getContext().getBinlogFileName() + " position: "
					+ server.getContext().getBinlogStartPos());
			return true;
		}
		return false;
	}

	@Override
	public void remove(Long taskId) {
		serverTasks.remove(taskId);
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
		for (Map.Entry<Long, Server> item : serverTasks.entrySet()) {
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
					log.error("Start server: " + server.getServerName()+ " failed.", e);
				}
			}
		}, server.getServerName() + "_Connector", false).start();
	}

	public BinlogPositionHolder getBinlogPositionHolder() {
		return binlogPositionHolder;
	}

	@Override
	public void startEvent(ReplicationTaskStatusEvent event) {
		if (serverTasks != null && serverTasks.containsKey(event.getTaskId())) {
			Server task = serverTasks.get(event.getTaskId());
			task.setStatusExecutorType(StatusExecutorType.PREPARING);
			startServer(task);
			task.setStatusExecutorType(StatusExecutorType.RUNNING);
		}
	}

	@Override
	public void stopEvent(ReplicationTaskStatusEvent event) {
		if (serverTasks != null) {
			if (serverTasks.containsKey(event.getTaskId())) {
				Server task = serverTasks.get(event.getTaskId());
				try {
					task.setStatusExecutorType(StatusExecutorType.STOPPING);
					task.stop();
					task.setStatusExecutorType(StatusExecutorType.STOPPED);
					log.info("Server " + task.getServerName() + " stopped.");
				} catch (Exception e) {
					log.error("Stop Server" + task.getServerName() + " failed.", e);
					task.setStatusExecutorType(StatusExecutorType.FAILED);
				}
			}
		}
	}

	@Override
	public void restartEvent(ReplicationTaskStatusEvent event) {
		if (serverTasks != null) {
			if (serverTasks.containsKey(event.getTaskId())) {
				Server task = serverTasks.get(event.getTaskId());
				if (task.getStatusExecutorType() == StatusExecutorType.STOPPED
						|| task.getStatusExecutorType() == StatusExecutorType.FAILED) {
					task.setStatusExecutorType(StatusExecutorType.PREPARING);
					startServer(task);
					task.setStatusExecutorType(StatusExecutorType.RUNNING);
				}
			}
		}
	}

	@Override
	public void addEvent(ReplicationTaskEvent event) {
		if (!serverTasks.containsKey(event.getTaskId())) {
			ReplicationTask serverTask = replicationTaskService.find(event
					.getTaskId());
			Server task =null;
			try {
				task = construct(serverTask);
				serverTasks.put(task.getServerId(), task);
				task.setStatusExecutorType(StatusExecutorType.PREPARING);
				initContext(task);
				startServer(task);
				task.setStatusExecutorType(StatusExecutorType.RUNNING);
				log.info("Server " + task.getServerName() + " started at binlogFile: "
						+ task.getContext().getBinlogFileName() + " position: "
						+ task.getContext().getBinlogStartPos());
			} catch (Exception e) {
				log.error("add Server" + task.getServerName() + " failed.",e);
				e.printStackTrace();
			}
		}
	}

	@Override
	public void deleteEvent(ReplicationTaskEvent event) {
		if (serverTasks != null&&serverTasks.containsKey(event.getTaskId())) {
			Server task = serverTasks.get(event.getTaskId());
			try {
				task.setStatusExecutorType(StatusExecutorType.STOPPING);
				task.stop();
				task.setStatusExecutorType(StatusExecutorType.STOPPED);
				serverTasks.remove(event.getTaskId());
			} catch (Exception e) {
				log.error("delete Server" + task.getServerName() + " failed.",e);
				e.printStackTrace();
			}
		}
	}

	@Override
	public void updateEvent(ReplicationTaskEvent event) {
		if (serverTasks != null&&serverTasks.containsKey(event.getTaskId())) {
			Server task = serverTasks.get(event.getTaskId());
			try {
				task.setStatusExecutorType(StatusExecutorType.STOPPING);
				task.stop();
				task.setStatusExecutorType(StatusExecutorType.STOPPED);
				serverTasks.remove(event.getTaskId());
			} catch (Exception e) {
				log.error("delete Server" + task.getServerName() + " failed.",e);
				e.printStackTrace();
			}
			ReplicationTask serverTask = replicationTaskService.find(event
					.getTaskId());
			try {
				task.setStatusExecutorType(StatusExecutorType.PREPARING);
				task = construct(serverTask);
				serverTasks.put(task.getServerId(), task);
				initContext(task);
				startServer(task);
				task.setStatusExecutorType(StatusExecutorType.RUNNING);

			} catch (Exception e) {
				log.error("start Server" + task.getServerName() + " failed.",e);
				e.printStackTrace();
			}
			
		}
	}
	
	public static EventStorage getTaskStorage(String storageName){
		return taskStorage.get(storageName);
	}

}
