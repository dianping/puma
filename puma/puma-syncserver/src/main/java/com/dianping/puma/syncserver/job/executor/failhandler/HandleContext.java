package com.dianping.puma.syncserver.job.executor.failhandler;

import java.sql.SQLException;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.sync.model.task.Task;
import com.dianping.puma.syncserver.mysql.MysqlExecutor;

public class HandleContext {
    private ChangedEvent changedEvent;
    private SQLException sqlException;
    private MysqlExecutor mysqlExecutor;
    private Task task;

    public ChangedEvent getChangedEvent() {
        return changedEvent;
    }

    public void setChangedEvent(ChangedEvent changedEvent) {
        this.changedEvent = changedEvent;
    }

    public SQLException getSqlException() {
        return sqlException;
    }

    public void setSqlException(SQLException sqlException) {
        this.sqlException = sqlException;
    }

    public MysqlExecutor getMysqlExecutor() {
        return mysqlExecutor;
    }

    public void setMysqlExecutor(MysqlExecutor mysqlExecutor) {
        this.mysqlExecutor = mysqlExecutor;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

}
