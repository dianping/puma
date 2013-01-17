package com.dianping.puma.admin.service;

import com.dianping.puma.core.sync.model.task.DumpTask;

public interface DumpActionService {

    /**
     * 创建DumpAction，同时创建DumpActionState
     */
    Long create(DumpTask dumpAction);

    DumpTask find(Long id);

    void updateSyncTaskId(Long dumpActionId, Long syncTaskId);
}
