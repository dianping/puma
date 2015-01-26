package com.dianping.puma.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.puma.ComponentContainer;
import com.dianping.puma.bo.PositionInfo;
import com.dianping.puma.bo.PumaContext;
import com.dianping.puma.core.codec.JsonEventCodec;
import com.dianping.puma.core.monitor.NotifyService;
import com.dianping.puma.core.server.model.FileSenderConfig;
import com.dianping.puma.core.server.model.PumaServerDetailConfig;
import com.dianping.puma.core.util.PumaThreadUtils;
import com.dianping.puma.datahandler.DefaultDataHandler;
import com.dianping.puma.datahandler.DefaultTableMetaInfoFetcher;
import com.dianping.puma.parser.DefaultBinlogParser;
import com.dianping.puma.parser.Parser;
import com.dianping.puma.sender.FileDumpSender;
import com.dianping.puma.sender.Sender;
import com.dianping.puma.sender.dispatcher.SimpleDispatherImpl;
import com.dianping.puma.service.impl.PumaServerConfigServiceImpl;
import com.dianping.puma.storage.DefaultArchiveStrategy;
import com.dianping.puma.storage.DefaultCleanupStrategy;
import com.dianping.puma.storage.DefaultEventStorage;
import com.dianping.puma.storage.LocalFileBucketIndex;

public class DefaultServerManager implements ServerManager {

	private static Logger log = Logger.getLogger(ServerManager.class);

	private static List<Server> servers = null;

	@Autowired
	PumaServerConfigServiceImpl pumaServerConfigService;

	private static final String BEAN_WEBAPPCONFIG = "webAppConfig";
	private static final String BEAN_NOTIFYSERVICE = "notifyService";
	private static final String BEAN_BINLOGPOSITIONHOLDER = "binlogPositionHolder";
	private static final String BEAN_JSONCODEC = "jsonCodec";

	private String webAppName;

	private BinlogPositionHolder binlogPositionHolder = null;

	static NotifyService notifyService = null;

	static JsonEventCodec jsonCodec = null;

	@Override
	public void init() {
		webAppName = ComponentContainer.SPRING.lookup(BEAN_WEBAPPCONFIG);
		notifyService = ComponentContainer.SPRING.lookup(BEAN_NOTIFYSERVICE);
		binlogPositionHolder = ComponentContainer.SPRING
				.lookup(BEAN_BINLOGPOSITIONHOLDER);
		jsonCodec = ComponentContainer.SPRING.lookup(BEAN_JSONCODEC);
	}

	@Override
	public List<Server> constructServers() throws Exception {
		List<PumaServerDetailConfig> pumaServerDetailConfigs = getServerDetailConfig(webAppName);
		if (pumaServerDetailConfigs != null
				&& pumaServerDetailConfigs.size() > 0) {
			servers = new ArrayList<Server>();
			Server server = null;
			for (PumaServerDetailConfig config : pumaServerDetailConfigs) {
				server = construct(config);
				servers.add(server);
			}
		}
		return servers;
	}

	@Override
	public Server construct(PumaServerDetailConfig config)
			throws Exception {
		ReplicationBasedServer server = new ReplicationBasedServer();
		server.setNotifyService(notifyService);
		server.setName(config.getServerName());
		server.setServerId(config.getServerId());
		server.setHost(config.getDbHost());
		server.setPort(config.getDbPort());
		server.setUser(config.getDbUser());
		server.setPassword(config.getDbPassword());
		server.setDefaultBinlogFileName(config.getDefaultBinlogFileName());
		server.setDefaultBinlogPosition(config.getDefaultBinlogPosition());
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
				storage.setMasterBucketIndex(slaveBucketIndex);
				// archive strategy
				DefaultArchiveStrategy archiveStrategy = new DefaultArchiveStrategy();
				archiveStrategy.setServerName(config.getServerName());
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
				senders.add(sender);
			}
		}
		dispatcher.setSenders(senders);
		dispatcher.start();
		server.setDispatcher(dispatcher);
		return server;
	}

	@Override
	public List<Server> getServers() {
		return servers;
	}
	
	@Override
	public int indexOf(String serverName) {
		for (int index = 0; index < servers.size(); index++) {
			if (servers.get(index).getServerName().equals(serverName)) {
				return index;
			}
		}
		return -1;
	}
	
	@Override
	public boolean contain(String serverName) {
		for (Server server : servers) {
			if (server.getServerName().equals(serverName)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean add(Server server) {
		if (server != null && !servers.contains(server)) {
			initContext(server);
			start(server);
			servers.add(server);
			log.info("Server " + server.getServerName()
					+ " started at binlogFile: "
					+ server.getContext().getBinlogFileName() + " position: "
					+ server.getContext().getBinlogStartPos());
			return true;
		}
		return false;
	}

	@Override
	public void remove(String serverName) {
		Iterator<Server> iterator = servers.iterator();
		Server server = null;
		while (iterator.hasNext()) {
			server = iterator.next();
			if (server.getServerName().equals(serverName)) {
				stop(server);
				iterator.remove();
				log.info("Server " + server.getServerName() + " removed.");
			}
		}
	}

	private List<PumaServerDetailConfig> getServerDetailConfig(String webAppName) {
		return pumaServerConfigService.find(webAppName);
	}

	@Override
	public void initContext(Server server) {
		String serverName = server.getServerName();
		PositionInfo posInfo = binlogPositionHolder.getPositionInfo(serverName,
				server.getDefaultBinlogFileName(), server
						.getDefaultBinlogPosition());
		PumaContext context = new PumaContext();

		context.setPumaServerId(server.getServerId());
		context.setPumaServerName(serverName);
		context.setBinlogFileName(posInfo.getBinlogFileName());
		context.setBinlogStartPos(posInfo.getBinlogPosition());
		server.setContext(context);
	}

	@Override
	public void stop(Server server) {
		try {
			server.stop();
			log.info("Server " + server.getServerName() + " stopped.");
		} catch (Exception e) {
			log.error("Stop Server" + server.getServerName() + " failed.", e);
		}
	}

	@Override
	public void stopServers() {
		log.info("Stopping...");
		for (Server server : servers) {
			stop(server);
		}
	}

	@Override
	public void start(final Server server) {
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

	public void setWebAppName(String webAppName) {
		this.webAppName = webAppName;
	}

	public String getWebAppName() {
		return webAppName;
	}

	public BinlogPositionHolder getBinlogPositionHolder() {
		return binlogPositionHolder;
	}
}
