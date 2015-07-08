package com.dianping.puma.server.builder.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.dianping.puma.biz.entity.PumaTask;
import com.dianping.puma.biz.entity.SrcDBInstance;
import com.dianping.puma.biz.service.SrcDBInstanceService;
import com.dianping.puma.codec.RawEventCodec;
import com.dianping.puma.config.PumaServerConfig;
import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.model.BinlogStat;
import com.dianping.puma.core.model.event.EventCenter;
import com.dianping.puma.core.model.state.PumaTaskState;
import com.dianping.puma.core.storage.holder.BinlogInfoHolder;
import com.dianping.puma.core.util.sql.DDLType;
import com.dianping.puma.datahandler.DefaultDataHandler;
import com.dianping.puma.filter.DDLEventFilter;
import com.dianping.puma.filter.DMLEventFilter;
import com.dianping.puma.filter.DefaultEventFilterChain;
import com.dianping.puma.filter.EventFilter;
import com.dianping.puma.filter.EventFilterChain;
import com.dianping.puma.filter.TableMetaRefreshFilter;
import com.dianping.puma.filter.TransactionEventFilter;
import com.dianping.puma.meta.DefaultTableMetaInfoFectcher;
import com.dianping.puma.meta.TableMetaInfoStore;
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

@Service("taskExecutorBuilder")
public class DefaultTaskExecutorBuilder implements TaskExecutorBuilder {

    @Autowired
    SrcDBInstanceService srcDBInstanceService;

    @Autowired
    BinlogInfoHolder binlogInfoHolder;

    @Autowired
    EventCenter eventCenter;

    @Autowired
    PumaServerConfig pumaServerConfig;

    @Autowired
    private RawEventCodec rawCodec;

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

            PumaTaskState taskState = new PumaTaskState();
            taskState.setName(pumaTask.getName());
            taskState.setServerName(pumaServerConfig.getName());
            taskState.setTaskName(pumaTask.getName());
            taskState.setStatus(Status.PREPARING);
            taskExecutor.setTaskState(taskState);

            // Base.
            String taskName = pumaTask.getName();
            taskExecutor.setTaskName(taskName);

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
            DefaultTableMetaInfoFectcher tableMetaInfo = new DefaultTableMetaInfoFectcher();
            tableMetaInfo.setSrcDbInstance(srcDBInstance);
            BinlogInfo binlogInfo1 = new BinlogInfo("mysql-bin.000000", 4L);
      		tableMetaInfo.setBinlogInfo(binlogInfo1);
            
      		TableMetaInfoStore metaStore = new TableMetaInfoStore();
      		metaStore.start();
      		
      		tableMetaInfo.setTableMetaInfoStore(metaStore);
            // tableMeta refresh filter
            TableMetaRefreshFilter tableMetaRefreshFilter = new TableMetaRefreshFilter();
            tableMetaRefreshFilter.setName(taskName);
            eventCenter.register(tableMetaRefreshFilter);
            tableMetaInfo.setTableMetaRefreshFilter(tableMetaRefreshFilter);

            dataHandler.setTableMetasInfoFetcher(tableMetaInfo);
            dataHandler.start();
            taskExecutor.setDataHandler(dataHandler);

            // File sender.
            List<Sender> senders = new ArrayList<Sender>();
            FileDumpSender sender = new FileDumpSender();
            sender.setName(fileSenderName + taskName);

            // File sender storage.
            DefaultEventStorage storage = new DefaultEventStorage();
            storage.setName(storageName + taskName);
            storage.setTaskName(taskName);

            // storage.setAcceptedDataTables(pumaTask.getAcceptedDataInfos());
            storage.setCodec(rawCodec);

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