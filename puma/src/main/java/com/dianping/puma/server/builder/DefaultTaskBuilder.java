package com.dianping.puma.server.builder;

import com.dianping.puma.biz.entity.PumaTaskStateEntity;
import com.dianping.puma.core.util.sql.DDLType;
import com.dianping.puma.datahandler.DefaultDataHandler;
import com.dianping.puma.filter.*;
import com.dianping.puma.instance.InstanceManager;
import com.dianping.puma.model.Schema;
import com.dianping.puma.model.SchemaSet;
import com.dianping.puma.model.Table;
import com.dianping.puma.model.TableSet;
import com.dianping.puma.parser.DefaultBinlogParser;
import com.dianping.puma.parser.Parser;
import com.dianping.puma.parser.meta.DefaultTableMetaInfoFetcher;
import com.dianping.puma.sender.FileDumpSender;
import com.dianping.puma.sender.Sender;
import com.dianping.puma.sender.dispatcher.SimpleDispatcherImpl;
import com.dianping.puma.storage.manage.InstanceStorageManager;
import com.dianping.puma.taskexecutor.DefaultTaskExecutor;
import com.dianping.puma.taskexecutor.TaskExecutor;
import com.dianping.puma.taskexecutor.task.DatabaseTask;
import com.dianping.puma.taskexecutor.task.InstanceTask;
import com.dianping.puma.utils.IPUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("taskBuilder")
public class DefaultTaskBuilder implements TaskBuilder {

    private final Logger logger = LoggerFactory.getLogger(DefaultTaskBuilder.class);

    @Autowired
    private InstanceStorageManager instanceStorageManager;

    @Autowired
    private InstanceManager instanceManager;

    public TaskExecutor build(InstanceTask instanceTask) {
        logger.info("start building puma task executor...\n{}", instanceTask);

        DefaultTaskExecutor taskExecutor = new DefaultTaskExecutor();

        taskExecutor.setInstanceTask(instanceTask);

        String taskName = instanceTask.getTaskName();
        taskExecutor.setTaskName(taskName);
        taskExecutor.setServerId((taskName + IPUtils.getFirstNoLoopbackIP4Address()).hashCode());
        taskExecutor.setBeginTime(instanceTask.getDatabaseTasks().get(0).getBeginTime());

        TableSet tableSet = new TableSet();
        for (DatabaseTask databaseTask : instanceTask.getDatabaseTasks()) {
            String database = databaseTask.getDatabase();
            List<String> tables = databaseTask.getTables();
            for (String table : tables) {
                tableSet.add(new Table(database, table));
            }
        }
        taskExecutor.setTableSet(tableSet);

        taskExecutor.setInstanceManager(instanceManager);

        PumaTaskStateEntity taskState = new PumaTaskStateEntity();
        taskState.setTaskName(taskName);
        taskExecutor.setTaskState(taskState);

        taskExecutor.setInstanceStorageManager(instanceStorageManager);

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
        SchemaSet schemaSet = new SchemaSet();
        for (Table table : tableSet.listSchemaTables()) {
            schemaSet.add(new Schema(table.getSchemaName()));
        }
        transactionEventFilter.setAcceptedSchemas(schemaSet);
        eventFilterList.add(transactionEventFilter);

        eventFilterChain.setEventFilters(eventFilterList);

        // File sender.
        List<Sender> senders = new ArrayList<Sender>();
        FileDumpSender sender = new FileDumpSender();
        sender.setName("fileSender-" + taskName);
        sender.setStorageEventFilterChain(eventFilterChain);
        senders.add(sender);

        // Dispatch.
        SimpleDispatcherImpl dispatcher = new SimpleDispatcherImpl();
        dispatcher.setName("dispatch-" + taskName);
        dispatcher.setSenders(senders);
        taskExecutor.setDispatcher(dispatcher);
        taskExecutor.initContext();

        return taskExecutor;
    }
}
