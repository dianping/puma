package com.dianping.puma.syncserver.job.executor.failhandler;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent.ColumnInfo;
import com.dianping.puma.core.monitor.NotifyService;
import com.dianping.puma.syncserver.mysql.MysqlExecutor;

public class DeleteDuplicateRecordHandler implements Handler {
    private static final Logger LOG = LoggerFactory.getLogger(DeleteDuplicateRecordHandler.class);

    protected MysqlExecutor mysqlExecutor;

    @Autowired
    private NotifyService notifyService;

    @Override
    public String getName() {
        return "DeleteDuplicateRecord";
    }

    @Override
    public HandleResult handle(HandleContext context) {
        HandleResult result = new HandleResult();
        result.setIgnoreFailEvent(false);
        MysqlExecutor mysqlExecutor = context.getMysqlExecutor();
        ChangedEvent changedEvent0 = context.getChangedEvent();
        RowChangedEvent changedEvent = null;
        if (changedEvent0 instanceof RowChangedEvent) {//只处理RowChangedEvent
            changedEvent = (RowChangedEvent) changedEvent0;
            try {
                //构造删除event，并执行event，删除dest的对应重复了的记录
                RowChangedEvent deleteChangedEvent = getDeleteEvent(changedEvent);
                mysqlExecutor.execute(deleteChangedEvent);
                notifyService.alarm("Handle(" + getName() + ") execute event:" + deleteChangedEvent.toString(), null, true);
                //重新尝试插入该event
                mysqlExecutor.execute(changedEvent);
                notifyService.alarm("Handle(" + getName() + ") execute event:" + changedEvent.toString(), null, true);
                //成功处理
                result.setIgnoreFailEvent(true);
            } catch (SQLException e) {
                String msg = "Unexpected SQLException on handler(" + getName() + "), ignoreFailEvent still false.";
                notifyService.alarm(msg, e, true);
                LOG.error(msg, e);
            }
        }
        return result;
    }

    private RowChangedEvent getDeleteEvent(RowChangedEvent changedEvent) {
        RowChangedEvent deleteChangedEvent = new RowChangedEvent();
        deleteChangedEvent.setActionType(RowChangedEvent.DELETE);
        deleteChangedEvent.setDatabase(changedEvent.getDatabase());
        deleteChangedEvent.setTable(changedEvent.getTable());
        Map<String, ColumnInfo> columns = new HashMap<String, RowChangedEvent.ColumnInfo>(changedEvent.getColumns());
        Iterator<Map.Entry<String, ColumnInfo>> iterator = columns.entrySet().iterator();
        while (iterator.hasNext()) {//删除非key的Column
            Map.Entry<String, ColumnInfo> entry = iterator.next();
            ColumnInfo columnInfo = entry.getValue();
            if (!columnInfo.isKey()) {
                iterator.remove();
            }
        }
        deleteChangedEvent.setColumns(columns);
        deleteChangedEvent.setDatabase(deleteChangedEvent.getDatabase());
        return deleteChangedEvent;
    }
}
