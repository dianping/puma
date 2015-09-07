package com.dianping.puma.server.builder;

import java.util.ArrayList;
import java.util.List;

import com.dianping.puma.core.config.ConfigManager;
import com.dianping.puma.core.model.*;
import com.dianping.puma.instance.InstanceManager;
import com.dianping.puma.taskexecutor.task.DatabaseTask;
import com.dianping.puma.taskexecutor.task.InstanceTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.biz.entity.PumaTaskEntity;
import com.dianping.puma.biz.entity.PumaTaskStateEntity;
import com.dianping.puma.core.codec.RawEventCodec;
import com.dianping.puma.core.constant.Status;
import com.dianping.puma.core.util.sql.DDLType;
import com.dianping.puma.datahandler.DefaultDataHandler;
import com.dianping.puma.filter.DDLEventFilter;
import com.dianping.puma.filter.DMLEventFilter;
import com.dianping.puma.filter.DefaultEventFilterChain;
import com.dianping.puma.filter.EventFilter;
import com.dianping.puma.filter.EventFilterChain;
import com.dianping.puma.filter.TransactionEventFilter;
import com.dianping.puma.parser.DefaultBinlogParser;
import com.dianping.puma.parser.Parser;
import com.dianping.puma.parser.meta.DefaultTableMetaInfoFetcher;
import com.dianping.puma.sender.FileDumpSender;
import com.dianping.puma.sender.Sender;
import com.dianping.puma.sender.dispatcher.SimpleDispatcherImpl;
import com.dianping.puma.storage.holder.BinlogInfoHolder;
import com.dianping.puma.taskexecutor.DefaultTaskExecutor;
import com.dianping.puma.taskexecutor.TaskExecutor;

@Service("taskBuilder")
public class DefaultTaskBuilder implements TaskBuilder {

    @Autowired
    BinlogInfoHolder binlogInfoHolder;

    @Autowired
    InstanceManager instanceManager;

    @Autowired
    ConfigManager configManager;

    @Autowired
    RawEventCodec rawCodec;

    public TaskExecutor build(InstanceTask instanceTask) {
        DefaultTaskExecutor taskExecutor = new DefaultTaskExecutor();

        taskExecutor.setInstanceTask(instanceTask);

        String taskName = instanceTask.getTaskName();
        taskExecutor.setTaskName(taskName);
        taskExecutor.setServerId(taskName.hashCode());
        taskExecutor.setBeginTime(instanceTask.getDatabaseTasks().get(0).getBeginTime());

        TableSet tableSet = new TableSet();
        for (DatabaseTask databaseTask: instanceTask.getDatabaseTasks()) {
            String database = databaseTask.getDatabase();
            List<String> tables = databaseTask.getTables();
            for (String table: tables) {
                tableSet.add(new Table(database, table));
            }
        }
        taskExecutor.setTableSet(tableSet);

        taskExecutor.setInstanceManager(instanceManager);

        PumaTaskStateEntity taskState = new PumaTaskStateEntity();
        taskState.setTaskName(taskName);
        taskState.setStatus(Status.PREPARING);
        taskExecutor.setTaskState(taskState);

        taskExecutor.setBinlogInfoHolder(binlogInfoHolder);
        taskExecutor.setBinlogStat(new BinlogStat());

        // Parser.
        Parser parser = new DefaultBinlogParser();
        taskExecutor.setParser(parser);

        // Handler.
        DefaultDataHandler dataHandler = new DefaultDataHandler();
        DefaultTableMetaInfoFetcher tableMetaInfo = new DefaultTableMetaInfoFetcher();
        tableMetaInfo.setName(taskName);
        tableMetaInfo.setAcceptedTables(tableSet);
        taskExecutor.setTableMetaInfoFetcher(tableMetaInfo);
        dataHandler.setTableMetasInfoFetcher(tableMetaInfo);
        taskExecutor.setDataHandler(dataHandler);

        // Filters.
        EventFilterChain eventFilterChain = new DefaultEventFilterChain();
        List<EventFilter> eventFilterList = new ArrayList<EventFilter>();

        // DML event filter.
        DMLEventFilter dmlEventFilter = new DMLEventFilter();
        dmlEventFilter.setName(taskName);
        dmlEventFilter.setDml(true);

        dmlEventFilter.setAcceptedTables(tableSet);
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
        eventFilterList.add(ddlEventFilter);

        // Transaction event filter.
        TransactionEventFilter transactionEventFilter = new TransactionEventFilter();
        transactionEventFilter.setName(taskName);
        transactionEventFilter.setBegin(true);
        transactionEventFilter.setCommit(true);
        if (tableSet != null) {
            SchemaSet schemaSet = new SchemaSet();
            for (Table table : tableSet.listSchemaTables()) {
                schemaSet.add(new Schema(table.getSchemaName()));
            }
            transactionEventFilter.setAcceptedSchemas(schemaSet);
        }
        eventFilterList.add(transactionEventFilter);

        eventFilterChain.setEventFilters(eventFilterList);

        // File sender.
        List<Sender> senders = new ArrayList<Sender>();
        FileDumpSender sender = new FileDumpSender();
        sender.setName("fileSender-" + taskName);
        sender.setTaskName(taskName);
        sender.setCodec(rawCodec);
        sender.setStorageEventFilterChain(eventFilterChain);
        sender.setPreservedDay(5);
        senders.add(sender);

        // Dispatch.
        SimpleDispatcherImpl dispatcher = new SimpleDispatcherImpl();
        dispatcher.setName("dispatch-" + taskName);
        dispatcher.setSenders(senders);
        taskExecutor.setDispatcher(dispatcher);
        taskExecutor.initContext();

        // Set puma task status.
        taskExecutor.getTaskState().setStatus(Status.WAITING);

        return taskExecutor;
    }

    public TaskExecutor build(PumaTaskEntity pumaTask) throws Exception {
        DefaultTaskExecutor taskExecutor = new DefaultTaskExecutor();

        taskExecutor.setTask(pumaTask);

        PumaTaskStateEntity taskState = new PumaTaskStateEntity();
        taskState.setTaskName(pumaTask.getName());
        taskState.setStatus(Status.PREPARING);

        taskExecutor.setTaskState(taskState);

        // Base.
        String taskName = pumaTask.getName();
        taskExecutor.setTaskName(taskName);
        taskExecutor.setServerId(taskName.hashCode() + "self".hashCode());

        // Bin log.
        taskExecutor.setBinlogInfoHolder(binlogInfoHolder);
        taskExecutor.setBinlogStat(new BinlogStat());

        // Parser.
        Parser parser = new DefaultBinlogParser();

        taskExecutor.setParser(parser);

        // Handler.
        DefaultDataHandler dataHandler = new DefaultDataHandler();
        DefaultTableMetaInfoFetcher tableMetaInfo = new DefaultTableMetaInfoFetcher();
        tableMetaInfo.setAcceptedTables(pumaTask.getTableSet());
        taskExecutor.setTableMetaInfoFetcher(tableMetaInfo);

        dataHandler.setTableMetasInfoFetcher(tableMetaInfo);

        taskExecutor.setDataHandler(dataHandler);

        EventFilterChain eventFilterChain = new DefaultEventFilterChain();
        List<EventFilter> eventFilterList = new ArrayList<EventFilter>();

        // DML event filter.
        DMLEventFilter dmlEventFilter = new DMLEventFilter();
        dmlEventFilter.setName(taskName);
        dmlEventFilter.setDml(true);
        dmlEventFilter.setAcceptedTables(pumaTask.getTableSet());
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
        eventFilterList.add(transactionEventFilter);

        eventFilterChain.setEventFilters(eventFilterList);

        // File sender.
        List<Sender> senders = new ArrayList<Sender>();
        FileDumpSender sender = new FileDumpSender();
        sender.setName("fileSender-" + taskName);
        sender.setTaskName(taskName);
        sender.setCodec(rawCodec);
        sender.setStorageEventFilterChain(eventFilterChain);
        sender.setPreservedDay(pumaTask.getPreservedDay());

        senders.add(sender);

        // Dispatch.
        SimpleDispatcherImpl dispatcher = new SimpleDispatcherImpl();
        dispatcher.setName("dispatch-" + taskName);
        dispatcher.setSenders(senders);
        taskExecutor.setDispatcher(dispatcher);

        taskExecutor.initContext();

        // Set puma task status.
        taskExecutor.getTaskState().setStatus(Status.WAITING);

        return taskExecutor;
    }
}
