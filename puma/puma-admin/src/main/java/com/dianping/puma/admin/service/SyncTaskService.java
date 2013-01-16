package com.dianping.puma.admin.service;

import java.util.List;

import com.dianping.puma.core.sync.SyncTask;

public interface SyncTaskService {

    Long saveSyncTask(SyncTask syncTask);

    //    SyncTask findSyncTask(ObjectId objectId);

    SyncTask findSyncTaskBySyncConfigId(Long objectId);

    List<SyncTask> findSyncTasksBySyncConfigId(List<Long> syncConfigIds);

    //    List<SyncTask> findSyncTasks(int offset, int limit);

    //    Long countSyncTasks();
}
