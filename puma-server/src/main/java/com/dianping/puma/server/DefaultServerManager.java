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
import com.dianping.puma.core.monitor.ServerTaskActionEvent;
import com.dianping.puma.core.server.model.FileSenderConfig;
import com.dianping.puma.core.server.model.ServerTask;
import com.dianping.puma.core.util.PumaThreadUtils;
import com.dianping.puma.datahandler.DefaultDataHandler;
import com.dianping.puma.datahandler.DefaultTableMetaInfoFetcher;
import com.dianping.puma.parser.DefaultBinlogParser;
import com.dianping.puma.parser.Parser;
import com.dianping.puma.sender.FileDumpSender;
import com.dianping.puma.sender.Sender;
import com.dianping.puma.sender.dispatcher.SimpleDispatherImpl;
import com.dianping.puma.service.ServerTaskService;
import com.dianping.puma.storage.DefaultArchiveStrategy;
import com.dianping.puma.storage.DefaultCleanupStrategy;
import com.dianping.puma.storage.DefaultEventStorage;
import com.dianping.puma.storage.LocalFileBucketIndex;

@Service("serverManager")
public class DefaultServerManager implements ServerManager {

	private static Logger log = Logger.getLogger(DefaultServerManager.class);

	private static ConcurrentHashMap<Long, Server> serverTasks = null;

	@Autowired
	private ServerTaskService serverTaskService;

	private String serverName;
	@Autowired
	private BinlogPositionHolder binlogPositionHolder = null;
	@Autowired
	private NotifyService notifyService = null;
	@Autowired
	private JsonEventCodec jsonCodec = null;
	@Autowired
	private InitializeServerConfig serverConfig = null;

	@PostConstruct
	public void init() {
		// notifyService = ComponentContainer.SPRING.lookup(BEAN_NOTIFYSERVICE);
		// binlogPositionHolder = ComponentContainer.SPRING
		// .lookup(BEAN_BINLOGPOSITIONHOLDER);
		// jsonCodec = ComponentContainer.SPRING.lookup(BEAN_JSONCODEC);
		serverName = serverConfig.getServerName();
	}

	@Override
	public ConcurrentHashMap<Long, Server> constructServers() throws Exception {
		log.info("starting construct servers.........");
		List<ServerTask> pumaServerTaskConfigs = getServerTask(serverName);
		if (pumaServerTaskConfigs != null && pumaServerTaskConfigs.size() > 0) {
			serverTasks = new ConcurrentHashMap<Long, Server>();
			Server server = null;
			for (ServerTask config : pumaServerTaskConfigs) {
				server = construct(config);
				serverTasks.put(server.getServerId(), server);
			}
		}
		log.info("ended construct servers.........");
		return serverTasks;
	}

	@Override
	public Server construct(ServerTask config) throws Exception {
		log.info("construct server " + config.getTaskName() + ".......");
		ReplicationBasedServer server = new ReplicationBasedServer();
		server.setNotifyService(notifyService);
		server.setName(config.getTaskName());
		server.setServerId(config.getTaskId());
		server.setHost(config.getDbHost());
		server.setPort(config.getDbPort());
		server.setUser(config.getDbUser());
		server.setPassword(config.getDbPassword());
		server.setDefaultBinlogFileName(config.getDefaultBinlogFileName());
		server.setDefaultBinlogPosition(config.getDefaultBinlogPosition());
		server.setBinlogPositionHolder(binlogPositionHolder);
		// server.setTaskActionStatus(ServerTaskActionStatus.NONE);
		// server.setTaskExecutorStatus(TaskExecutorStatus.WAITING);
		// parser
		Parser parser = new DefaultBinlogParser();
		parser.start();
		server.setParser(parser);
		// tableMetaInfo
		DefaultTableMetaInfoFetcher tableMetaInfo = new DefaultTableMetaInfoFetcher();
		tableMetaInfo.setMetaDBHost(config.getMetaDBHost());
		tableMetaInfo.setMetaDBPort(config.getMetaDBPort());
		tableMetaInfo.setMetaDBUser(config.getMetaDBUser());
		tableMetaInfo.setMetaDBPassword(config.getMetaDBPassword());
		// handler
		DefaultDataHandler dataHandler = new DefaultDataHandler();
		dataHandler.setNotifyService(notifyService);
		dataHandler.setTableMetasInfoFetcher(tableMetaInfo);
		dataHandler.start();
		server.setDataHandler(dataHandler);
		// dispatcher
		SimpleDispatherImpl dispatcher = new SimpleDispatherImpl();
		dispatcher.setName(config.getDispatcherName());
		// file senders
		List<Sender> senders = null;
		if (config.getFileSenders() != null) {
			senders = new ArrayList<Sender>();
			for (FileSenderConfig senderItem : config.getFileSenders()) {
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
				archiveStrategy.setServerName(config.getTaskName());
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

	private List<ServerTask> getServerTask(String serverName) {
		log.info("starting get detail config.........");
		return serverTaskService.find(serverName);
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
	public void startEvent(ServerTaskActionEvent event) {

	}

	@Override
	public void stopEvent(ServerTaskActionEvent event) {

	}

	@Override
	public void restartEvent(ServerTaskActionEvent event) {

	}

	@Override
	public void addEvent(ServerTaskActionEvent event) {

	}

	@Override
	public void deleteEvent(ServerTaskActionEvent event) {

	}

	@Override
	public void updateEvent(ServerTaskActionEvent event) {
	}

}
