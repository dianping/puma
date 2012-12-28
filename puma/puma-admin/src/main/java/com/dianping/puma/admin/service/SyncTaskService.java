package com.dianping.puma.admin.service;

import java.util.List;

import org.bson.types.ObjectId;

import com.dianping.puma.core.sync.SyncTask;

public interface SyncTaskService {

    ObjectId saveSyncTask(SyncTask syncTask);

    //    SyncTask findSyncTask(ObjectId objectId);

    SyncTask findSyncTaskBySyncConfigId(ObjectId objectId);

    List<SyncTask> findSyncTasksBySyncConfigId(List<ObjectId> syncConfigIds);

    //    List<SyncTask> findSyncTasks(int offset, int limit);

    //    Long countSyncTasks();
}
