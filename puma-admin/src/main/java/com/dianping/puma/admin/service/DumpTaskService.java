package com.dianping.puma.admin.service;

import com.dianping.puma.core.sync.model.task.DumpTask;

public interface DumpTaskService {

    Long create(DumpTask dumpTask);

    DumpTask find(Long id);

    void updateSyncTaskId(Long dumpTaskId, Long syncTaskId);
}
