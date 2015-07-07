package com.dianping.puma.server.builder.impl;

import com.dianping.puma.config.PumaServerConfig;
import com.dianping.puma.core.codec.JsonEventCodec;
import com.dianping.puma.core.constant.Status;
import com.dianping.puma.biz.entity.PumaTask;
import com.dianping.puma.biz.entity.SrcDBInstance;
import com.dianping.puma.core.model.event.EventCenter;
import com.dianping.puma.core.storage.holder.BinlogInfoHolder;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.BinlogStat;
import com.dianping.puma.core.model.state.PumaTaskState;
import com.dianping.puma.biz.monitor.NotifyService;
import com.dianping.puma.biz.service.PumaTaskStateService;
import com.dianping.puma.biz.service.SrcDBInstanceService;
import com.dianping.puma.core.util.sql.DDLType;
import com.dianping.puma.datahandler.DefaultDataHandler;
import com.dianping.puma.datahandler.DefaultTableMetaInfoFetcher;
import com.dianping.puma.filter.*;
import com.dianping.puma.monitor.FetcherEventCountMonitor;
import com.dianping.puma.monitor.ParserEventCountMonitor;
import com.dianping.puma.monitor.StorageEventCountMonitor;
import com.dianping.puma.monitor.StorageEventGroupMonitor;
import com.dianping.puma.parser.DefaultBinlogParser;
import com.dianping.puma.parser.Parser;
import com.dianping.puma.sender.FileDumpSender;
import com.dianping.puma.sender.Sender;
import com.dianping.puma.sender.dispatcher.SimpleDispatcherImpl;
import com.dianping.puma.server.DefaultTaskExecutor;
import com.dianping.puma.server.TaskExecutor;
import com.dianping.puma.server.builder.TaskExecutorBuilder;
import com.dianping.puma.storage.DefaultArchiveStrategy;
import com.dianping.puma.storage.DefaultCleanupStrategy;
import com.dianping.puma.storage.DefaultEventStorage;
import com.dianping.puma.storage.LocalFileBucketIndex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("taskExecutorBuilder")
public class DefaultTaskExecutorBuilder implements TaskExecutorBuilder {

	@Autowired
	private FetcherEventCountMonitor fetcherEventCountMonitor;
	@Autowired
	private ParserEventCountMonitor parserEventCountMonitor;
	@Autowired
	private StorageEventCountMonitor storageEventCountMonitor;
	@Autowired
	private StorageEventGroupMonitor storageEventGroupMonitor;

	@Autowired
	SrcDBInstanceService srcDBInstanceService;

	@Autowired
	BinlogInfoHolder binlogInfoHolder;

	@Autowired
	NotifyService notifyService;

	@Autowired
	EventCenter eventCenter;

	@Autowired
	PumaTaskStateService pumaTaskStateService;

	@Autowired
	PumaServerConfig pumaServerConfig;

	@Autowired
	private JsonEventCodec jsonCodec;

	@Value("fileSender-")
	String fileSenderName;

	@Value("storage-")
	String storageName;

	@Value("dispatch-")
	String dispatchName;

	@Value("/data/appdatas/puma/storage/master/")
	String masterStorageBaseDir;

	@Value("Bucket-")
	String masterBucketFilePrefix;

	@Value("1000")
	int maxMasterBucketLengthMB;

	@Value("25")
	int maxMasterFileCount;

	@Value("/data/appdatas/puma/storage/slave/")
	String slaveStorageBaseDir;

	@Value("Bucket-")
	String slaveBucketFilePrefix;

	@Value("1000")
	int maxSlaveBucketLengthMB;

	@Value("25")
	int maxSlaveFileCount;

	@Value("/data/appdatas/puma/binlogIndex/")
	String binlogIndexBaseDir;

	private static final Logger LOG = LoggerFactory.getLogger(DefaultTaskExecutorBuilder.class);

	public TaskExecutor build(PumaTask pumaTask) throws Exception {

		try {
			DefaultTaskExecutor taskExecutor = new DefaultTaskExecutor();
			taskExecutor.setFetcherEventCountMonitor(fetcherEventCountMonitor);
			taskExecutor.setParserEventCountMonitor(parserEventCountMonitor);

			PumaTaskState taskState = new PumaTaskState();
			taskState.setName(pumaTaskStateService.getStateName(pumaTask.getName(), pumaServerConfig.getName()));
			taskState.setServerName(pumaServerConfig.getName());
			taskState.setTaskName(pumaTask.getName());
			taskState.setStatus(Status.PREPARING);
			taskExecutor.setTaskState(taskState);

			// Base.
			String taskName = pumaTask.getName();
			taskExecutor.setTaskName(taskName);
			taskExecutor.setNotifyService(notifyService);

			taskExecutor.setServerId(taskName.hashCode() + pumaServerConfig.getName().hashCode());

			// Bin log.
			taskExecutor.setBinlogInfoHolder(binlogInfoHolder);
			taskExecutor.setBinlogInfo(pumaTask.getBinlogInfo());
			taskExecutor.setBinlogStat(new BinlogStat());

			// Source database.
			String srcDBInstanceName = pumaTask.getSrcDBInstanceName();
			SrcDBInstance srcDBInstance = srcDBInstanceService.find(srcDBInstanceName);
			taskExecutor.setDbServerId(srcDBInstance.getServerId());
			taskExecutor.setDBHost(srcDBInstance.getHost());
			taskExecutor.setPort(srcDBInstance.getPort());
			taskExecutor.setDBUsername(srcDBInstance.getUsername());
			taskExecutor.setDBPassword(srcDBInstance.getPassword());

			// Parser.
			Parser parser = new DefaultBinlogParser();
			// parser.start();
			taskExecutor.setParser(parser);

			// Handler.
			DefaultDataHandler dataHandler = new DefaultDataHandler();
			dataHandler.setNotifyService(notifyService);
			DefaultTableMetaInfoFetcher tableMetaInfo = new DefaultTableMetaInfoFetcher();
			// tableMetaInfo.setAcceptedDataTables(pumaTask.getAcceptedDataInfos());
			tableMetaInfo.setMetaDBHost(srcDBInstance.getMetaHost());
			tableMetaInfo.setMetaDBPort(srcDBInstance.getMetaPort());
			tableMetaInfo.setMetaDBUsername(srcDBInstance.getUsername());
			tableMetaInfo.setMetaDBPassword(srcDBInstance.getPassword());
			// tableMeta refresh filter
			TableMetaRefreshFilter tableMetaRefreshFilter = new TableMetaRefreshFilter();
			tableMetaRefreshFilter.setName(taskName);
			eventCenter.register(tableMetaRefreshFilter);
			tableMetaInfo.setTableMetaRefreshFilter(tableMetaRefreshFilter);

			dataHandler.setTableMetasInfoFetcher(tableMetaInfo);
			// dataHandler.start();
			taskExecutor.setDataHandler(dataHandler);

			// File sender.
			List<Sender> senders = new ArrayList<Sender>();
			FileDumpSender sender = new FileDumpSender();
			sender.setName(fileSenderName + taskName);
			sender.setNotifyService(notifyService);

			// File sender storage.
			DefaultEventStorage storage = new DefaultEventStorage();
			storage.setName(storageName + taskName);
			storage.setTaskName(taskName);

			// storage.setAcceptedDataTables(pumaTask.getAcceptedDataInfos());
			storage.setCodec(jsonCodec);
			storage.setStorageEventCountMonitor(storageEventCountMonitor);
			storage.setStorageEventGroupMonitor(storageEventGroupMonitor);

			EventFilterChain eventFilterChain = new DefaultEventFilterChain();
			List<EventFilter> eventFilterList = new ArrayList<EventFilter>();

			// DML event filter.
			DMLEventFilter dmlEventFilter = new DMLEventFilter();
			dmlEventFilter.setName(taskName);
			dmlEventFilter.setDml(true);
			eventCenter.register(dmlEventFilter);
			eventFilterList.add(dmlEventFilter);

			// DDL event filter.
			DDLEventFilter ddlEventFilter = new DDLEventFilter();
			ddlEventFilter.setName(taskName);
			ddlEventFilter.setDdl(true);
			List<DDLType> ddlTypes = new ArrayList<DDLType>();
			ddlTypes.add(DDLType.ALTER_TABLE);
			ddlTypes.add(DDLType.CREATE_INDEX);
			ddlTypes.add(DDLType.DROP_INDEX);
			ddlEventFilter.setDdlTypes(ddlTypes);
			eventCenter.register(ddlEventFilter);
			eventFilterList.add(ddlEventFilter);

			// Transaction event filter.
			TransactionEventFilter transactionEventFilter = new TransactionEventFilter();
			transactionEventFilter.setName(taskName);
			transactionEventFilter.setBegin(true);
			transactionEventFilter.setCommit(true);
			eventCenter.register(transactionEventFilter);
			eventFilterList.add(transactionEventFilter);

			eventFilterChain.setEventFilters(eventFilterList);
			storage.setStorageEventFilterChain(eventFilterChain);

			BinlogInfo binlogInfo = binlogInfoHolder.getBinlogInfo(taskName);
			if (binlogInfo != null) {
				storage.setBinlogInfo(binlogInfo);
			} else {
				storage.setBinlogInfo(pumaTask.getBinlogInfo());
			}

			// File sender master storage.
			LocalFileBucketIndex masterBucketIndex = new LocalFileBucketIndex();
			masterBucketIndex.setBaseDir(masterStorageBaseDir + taskName);
			masterBucketIndex.setBucketFilePrefix(masterBucketFilePrefix);
			masterBucketIndex.setMaxBucketLengthMB(maxMasterBucketLengthMB);
			// masterBucketIndex.start();
			storage.setMasterBucketIndex(masterBucketIndex);

			// File sender slave storage.
			LocalFileBucketIndex slaveBucketIndex = new LocalFileBucketIndex();
			slaveBucketIndex.setBaseDir(slaveStorageBaseDir + taskName);
			slaveBucketIndex.setBucketFilePrefix(slaveBucketFilePrefix);
			slaveBucketIndex.setMaxBucketLengthMB(maxSlaveBucketLengthMB);
			// slaveBucketIndex.start();
			storage.setSlaveBucketIndex(slaveBucketIndex);

			// Archive strategy.
			DefaultArchiveStrategy archiveStrategy = new DefaultArchiveStrategy();
			archiveStrategy.setServerName(taskName);
			archiveStrategy.setMaxMasterFileCount(maxMasterFileCount);
			storage.setArchiveStrategy(archiveStrategy);

			// Clean up strategy.
			DefaultCleanupStrategy cleanupStrategy = new DefaultCleanupStrategy();
			cleanupStrategy.setPreservedDay(pumaTask.getPreservedDay());
			storage.setCleanupStrategy(cleanupStrategy);

			storage.setBinlogIndexBaseDir(binlogIndexBaseDir + taskName);
			// storage.start();
			sender.setStorage(storage);
			// sender.start();
			senders.add(sender);

			// Dispatch.
			SimpleDispatcherImpl dispatcher = new SimpleDispatcherImpl();
			dispatcher.setName(dispatchName + taskName);
			dispatcher.setSenders(senders);
			// dispatcher.start();
			taskExecutor.setDispatcher(dispatcher);

			// Set puma task status.
			taskExecutor.setStatus(Status.WAITING);

			return taskExecutor;
		} catch (Exception e) {
			LOG.error("Build puma task `{}` error: {}.", pumaTask.getName(), e.getMessage());
			throw e;
		}
	}

}
