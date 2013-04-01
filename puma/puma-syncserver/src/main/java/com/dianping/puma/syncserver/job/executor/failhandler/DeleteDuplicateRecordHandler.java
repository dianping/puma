package com.dianping.puma.syncserver.job.executor.failhandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.dbutils.BasicRowProcessor;
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

    private BasicRowProcessor processor = new BasicRowProcessor();
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
            if (changedEvent.getActionType() == RowChangedEvent.INSERT) {//只处理INSERT事件
                try {
                    StringBuilder msgSB = new StringBuilder();
                    //查询重复的记录是什么，发邮件出来
                    RowChangedEvent selectEvent = getSelectEvent(changedEvent);
                    ResultSet resultSet = mysqlExecutor.execute(selectEvent);
                    if (resultSet != null) {
                        Map<String, Object> rowMap = processor.toMap(resultSet);
                        msgSB.append("Select the duplicate row: " + rowMap);
                    } else {
                        msgSB.append("Select the duplicate row return no result, select event is: " + selectEvent);
                    }
                    //这段代码也可以使用replace语法
                    //构造删除event，并执行event，删除dest的对应重复了的记录
                    RowChangedEvent deleteChangedEvent = getDeleteEvent(changedEvent);
                    mysqlExecutor.execute(deleteChangedEvent);
                    msgSB.append("||Delete Event: " + deleteChangedEvent.toString());
                    //重新尝试插入该event
                    mysqlExecutor.execute(changedEvent);
                    msgSB.append("||Insert Event: " + changedEvent.toString());
                    //成功处理
                    result.setIgnoreFailEvent(true);

                    notifyService.alarm("Handle(" + getName() + "): " + msgSB.toString(), null, false);
                } catch (SQLException e) {
                    String msg = "Unexpected SQLException on handler(" + getName() + "), ignoreFailEvent still false.";
                    notifyService.alarm(msg, e, true);
                    LOG.error(msg, e);
                }
            }
        }
        return result;
    }

    /**
     * 根据插入事件构造查询事件
     */
    private RowChangedEvent getSelectEvent(RowChangedEvent changedEvent) {
        RowChangedEvent selectEvent = new RowChangedEvent();
        selectEvent.setActionType(MysqlExecutor.SELECT);
        selectEvent.setDatabase(changedEvent.getDatabase());
        selectEvent.setTable(changedEvent.getTable());
        Map<String, ColumnInfo> columns = new HashMap<String, RowChangedEvent.ColumnInfo>(changedEvent.getColumns());
        selectEvent.setColumns(columns);
        return selectEvent;
    }

    /**
     * 根据插入事件构造删除事件
     */
    private RowChangedEvent getDeleteEvent(RowChangedEvent changedEvent) {
        RowChangedEvent deleteChangedEvent = new RowChangedEvent();
        deleteChangedEvent.setActionType(RowChangedEvent.DELETE);
        deleteChangedEvent.setDatabase(changedEvent.getDatabase());
        deleteChangedEvent.setTable(changedEvent.getTable());
        Map<String, ColumnInfo> columns = new HashMap<String, RowChangedEvent.ColumnInfo>(changedEvent.getColumns());
        Iterator<Map.Entry<String, ColumnInfo>> iterator = columns.entrySet().iterator();
        while (iterator.hasNext()) {//删除非key的Column，old值赋值为new值,new值删除
            Map.Entry<String, ColumnInfo> entry = iterator.next();
            ColumnInfo columnInfo = entry.getValue();
            columnInfo.setOldValue(columnInfo.getNewValue());
            columnInfo.setNewValue(null);
            if (!columnInfo.isKey()) {
                iterator.remove();
            }
        }
        deleteChangedEvent.setColumns(columns);
        return deleteChangedEvent;
    }
}
