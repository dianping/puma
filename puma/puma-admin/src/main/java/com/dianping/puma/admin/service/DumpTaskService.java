package com.dianping.puma.admin.service;

import com.dianping.puma.core.sync.model.task.DumpTask;

public interface DumpTaskService {

    /**
     * 创建Dumptask，同时创建DumptaskState
     */
    Long create(DumpTask dumpTask);

    DumpTask find(Long id);

    void updateSyncTaskId(Long dumpTaskId, Long syncTaskId);
}
