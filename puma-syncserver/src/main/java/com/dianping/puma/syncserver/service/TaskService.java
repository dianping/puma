package com.dianping.puma.syncserver.service;

import java.util.List;

import com.dianping.puma.core.entity.AbstractBaseSyncTask;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.sync.model.task.AbstractTask;
import com.dianping.puma.core.sync.model.task.SyncTask;
import com.dianping.puma.core.sync.model.task.Task;
import com.dianping.puma.core.sync.model.task.Type;

public interface TaskService {

    /**
     * 查询所有SyncTask
     */
    List<SyncTask> findSyncTasks(String syncServerName);

    /**
     * 根据type(sync,dump,catchup)和taskId,查询Task
     */
    <T extends Task> T find(Type type, long taskId);

    /**
     * 记录task的binlog
     */
    void recordBinlog(AbstractBaseSyncTask task, BinlogInfo binlogInfo);

}
