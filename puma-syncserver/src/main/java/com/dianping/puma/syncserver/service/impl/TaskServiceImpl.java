package com.dianping.puma.syncserver.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.dianping.puma.core.sync.model.task.*;
import com.dianping.puma.syncserver.service.BinlogInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.dao.task.CatchupTaskDao;
import com.dianping.puma.core.sync.dao.task.DumpTaskDao;
import com.dianping.puma.core.sync.dao.task.SyncTaskDao;
import com.dianping.puma.core.sync.model.BinlogInfo;
import com.dianping.puma.syncserver.service.TaskService;
import com.google.code.morphia.Key;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;

@Service
public class TaskServiceImpl implements TaskService {
    @Autowired
    SyncTaskDao syncTaskDao;
    @Autowired
    CatchupTaskDao catchupTaskDao;
    @Autowired
    DumpTaskDao dumpTaskDao;
    @Autowired
    BinlogInfoService binlogInfoService;

    @Override
    public List<SyncTask> findSyncTasks(String syncServerName) {
        Query<SyncTask> q = syncTaskDao.getDatastore().createQuery(SyncTask.class);
        q.filter("syncServerName", syncServerName);
        QueryResults<SyncTask> result = syncTaskDao.find(q);

        // Sync tasks stored in local files.
        List<String> localSyncTaskClientNames = binlogInfoService.findSyncTaskClientNames();
        // Sync tasks stored in remote MongoDB.
        List<SyncTask> remoteSyncTasks = result.asList();
        List<String> remoteSyncTaskClientNames = this.findSyncTaskClientNames(remoteSyncTasks);

        // Tasks found.
        List<SyncTask> syncTasks = new ArrayList<SyncTask>();

        // Old && Modified && New sync tasks.
        for (SyncTask remoteSyncTask: remoteSyncTasks) {
            String remoteSyncTaskClientName = remoteSyncTask.getPumaClientName();

            if (localSyncTaskClientNames.contains(remoteSyncTaskClientName)) {
                if (remoteSyncTask.getExecuted()) {
                    // Old sync tasks.
                    remoteSyncTask.setBinlogInfo(binlogInfoService.getBinlogInfo(remoteSyncTaskClientName));
                    syncTasks.add(remoteSyncTask);
                } else {
                    // @TODO
                    // Modified sync tasks.
                }
            } else {
                if (remoteSyncTask.getExecuted()) {
                    // @TODO
                    // Local file lost.
                } else {
                    // New sync tasks.
                    binlogInfoService.saveBinlogInfo(remoteSyncTaskClientName, remoteSyncTask.getBinlogInfo());
                    syncTasks.add(remoteSyncTask);
                    // Update mongoDB.
                    remoteSyncTask.setExecuted(true);
                    this.syncTaskDao.save(remoteSyncTask);
                }
            }
        }

        // Removed sync tasks.
        for (String localSyncTaskClientName: localSyncTaskClientNames) {
            if (!remoteSyncTaskClientNames.contains(localSyncTaskClientName)) {
                // Removed sync tasks.
                binlogInfoService.removeBinlogInfo(localSyncTaskClientName);
            }
        }

        return syncTasks;
    }

    private List<String> findSyncTaskClientNames(List<SyncTask> syncTasks) {
        List<String> syncTaskClientNames = new ArrayList<String>();
        for (SyncTask syncTask: syncTasks) {
            syncTaskClientNames.add(syncTask.getPumaClientName());
        }
        return syncTaskClientNames;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Task> T find(Type type, long taskId) {
        T task = null;
        switch (type) {
        case SYNC:
            task = (T) syncTaskDao.getDatastore().getByKey(SyncTask.class,
                  new Key<SyncTask>(SyncTask.class, taskId));
            task.setExecuted(true);
            this.syncTaskDao.save((SyncTask)task);

            if (task != null) {
                BinlogInfo binlogInfo = task.getBinlogInfo();
                binlogInfoService.saveBinlogInfo(((SyncTask) task).getPumaClientName(), binlogInfo);
            }

            break;
        case CATCHUP:
            task = (T) catchupTaskDao.getDatastore().getByKey(CatchupTask.class,
                  new Key<CatchupTask>(CatchupTask.class, taskId));
            task.setExecuted(true);
            this.catchupTaskDao.save((CatchupTask)task);

            if (task != null) {
                BinlogInfo binlogInfo = task.getBinlogInfo();
                binlogInfoService.saveBinlogInfo(((CatchupTask) task).getPumaClientName(), binlogInfo);
            }

            break;
        case DUMP:
            task = (T) dumpTaskDao.getDatastore().getByKey(DumpTask.class,
                  new Key<DumpTask>(DumpTask.class, taskId));
            task.setExecuted(true);
            this.dumpTaskDao.save((DumpTask)task);

            break;
        }

        return task;
    }

    @Override
    public void recordBinlog(AbstractTask abstractTask, BinlogInfo binlogInfo) {
        binlogInfoService.saveBinlogInfo(abstractTask.getPumaClientName(), binlogInfo);
    }
}