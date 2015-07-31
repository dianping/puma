package com.dianping.puma.server.builder;

import com.dianping.puma.biz.entity.PumaTaskEntity;
import com.dianping.puma.biz.entity.PumaTaskStateEntity;
import com.dianping.puma.core.codec.RawEventCodec;
import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.model.*;
import com.dianping.puma.core.model.event.EventCenter;
import com.dianping.puma.core.util.sql.DDLType;
import com.dianping.puma.datahandler.DefaultDataHandler;
import com.dianping.puma.filter.*;
import com.dianping.puma.parser.DefaultBinlogParser;
import com.dianping.puma.parser.Parser;
import com.dianping.puma.parser.meta.DefaultTableMetaInfoFetcher;
import com.dianping.puma.sender.FileDumpSender;
import com.dianping.puma.sender.Sender;
import com.dianping.puma.sender.dispatcher.SimpleDispatcherImpl;
import com.dianping.puma.server.server.TaskServerManager;
import com.dianping.puma.storage.DefaultArchiveStrategy;
import com.dianping.puma.storage.DefaultCleanupStrategy;
import com.dianping.puma.storage.DefaultEventStorage;
import com.dianping.puma.storage.bucket.LocalFileDataBucketManager;
import com.dianping.puma.storage.holder.BinlogInfoHolder;
import com.dianping.puma.taskexecutor.DefaultTaskExecutor;
import com.dianping.puma.taskexecutor.TaskExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("taskBuilder")
public class DefaultTaskBuilder implements TaskBuilder {

    @Autowired
    BinlogInfoHolder binlogInfoHolder;

    @Autowired
    EventCenter eventCenter;

    @Autowired
    TaskServerManager taskServerManager;

    @Autowired
    RawEventCodec rawCodec;

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

    public TaskExecutor build(PumaTaskEntity pumaTask) throws Exception {
        DefaultTaskExecutor taskExecutor = new DefaultTaskExecutor();

        taskExecutor.setTask(pumaTask);

        PumaTaskStateEntity taskState = new PumaTaskStateEntity();
        taskState.setTaskName(pumaTask.getName());
        taskState.setServerName("self");
        taskState.setStatus(Status.PREPARING);

        taskExecutor.setTaskState(taskState);

        // Base.
        String taskName = pumaTask.getName();
        taskExecutor.setTaskName(taskName);
        taskExecutor.setServerId(taskName.hashCode() + "self".hashCode());

        // Bin log.
        taskExecutor.setBinlogInfoHolder(binlogInfoHolder);
        taskExecutor.setBinlogInfo(pumaTask.getStartBinlogInfo());
        taskExecutor.setBinlogStat(new BinlogStat());

        // Parser.
        Parser parser = new DefaultBinlogParser();

        taskExecutor.setParser(parser);

        // Handler.
        DefaultDataHandler dataHandler = new DefaultDataHandler();
        DefaultTableMetaInfoFetcher tableMetaInfo = new DefaultTableMetaInfoFetcher();
        taskExecutor.setTableMetaInfoFetcher(tableMetaInfo);

        // tableMeta refresh filter
        TableMetaRefreshFilter tableMetaRefreshFilter = new TableMetaRefreshFilter();
        tableMetaRefreshFilter.setName(taskName);
        tableMetaRefreshFilter.setAcceptedTables(pumaTask.getTableSet());
        eventCenter.register(tableMetaRefreshFilter);
        tableMetaInfo.setTableMetaRefreshFilter(tableMetaRefreshFilter);

        dataHandler.setTableMetasInfoFetcher(tableMetaInfo);

        taskExecutor.setDataHandler(dataHandler);

        // File sender.
        List<Sender> senders = new ArrayList<Sender>();
        FileDumpSender sender = new FileDumpSender();
        sender.setName(fileSenderName + taskName);

        // File sender storage.
        DefaultEventStorage storage = new DefaultEventStorage();
        storage.setName(storageName + taskName);
        storage.setTaskName(taskName);

        storage.setCodec(rawCodec);

        EventFilterChain eventFilterChain = new DefaultEventFilterChain();
        List<EventFilter> eventFilterList = new ArrayList<EventFilter>();

        // DML event filter.
        DMLEventFilter dmlEventFilter = new DMLEventFilter();
        dmlEventFilter.setName(taskName);
        dmlEventFilter.setDml(true);
        dmlEventFilter.setAcceptedTables(pumaTask.getTableSet());
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
        if (pumaTask.getTableSet() != null) {
            SchemaSet schemaSet = new SchemaSet();
            for (Table table : pumaTask.getTableSet().listSchemaTables()) {
                schemaSet.add(new Schema(table.getSchemaName()));
            }
            transactionEventFilter.setAcceptedSchemas(schemaSet);
        }
        eventCenter.register(transactionEventFilter);
        eventFilterList.add(transactionEventFilter);

        eventFilterChain.setEventFilters(eventFilterList);
        storage.setStorageEventFilterChain(eventFilterChain);

        BinlogInfo binlogInfo = binlogInfoHolder.getBinlogInfo(taskName);
        if (binlogInfo != null) {
            storage.setBinlogInfo(binlogInfo);
        } else {
            storage.setBinlogInfo(pumaTask.getStartBinlogInfo());
        }

        // File sender master storage.
        LocalFileDataBucketManager masterBucketIndex = new LocalFileDataBucketManager();
        masterBucketIndex.setBaseDir(masterStorageBaseDir + taskName);
        masterBucketIndex.setBucketFilePrefix(masterBucketFilePrefix);
        masterBucketIndex.setMaxBucketLengthMB(maxMasterBucketLengthMB);

        storage.setMasterBucketIndex(masterBucketIndex);

        // File sender slave storage.
        LocalFileDataBucketManager slaveBucketIndex = new LocalFileDataBucketManager();
        slaveBucketIndex.setBaseDir(slaveStorageBaseDir + taskName);
        slaveBucketIndex.setBucketFilePrefix(slaveBucketFilePrefix);
        slaveBucketIndex.setMaxBucketLengthMB(maxSlaveBucketLengthMB);

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

        sender.setStorage(storage);

        senders.add(sender);

        // Dispatch.
        SimpleDispatcherImpl dispatcher = new SimpleDispatcherImpl();
        dispatcher.setName(dispatchName + taskName);
        dispatcher.setSenders(senders);
        taskExecutor.setDispatcher(dispatcher);

        taskExecutor.initContext();

        // Set puma task status.
        taskExecutor.getTaskState().setStatus(Status.WAITING);

        return taskExecutor;
    }
}
