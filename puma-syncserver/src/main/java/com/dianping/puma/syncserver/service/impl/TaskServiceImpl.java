package com.dianping.puma.syncserver.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.dianping.puma.syncserver.service.BinlogInfoService;
<<<<<<< HEAD
import net.sf.ehcache.concurrent.Sync;
=======
>>>>>>> 692a38483d8b86cc391587d855ef041a5ec1aacc
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.dao.task.CatchupTaskDao;
import com.dianping.puma.core.sync.dao.task.DumpTaskDao;
import com.dianping.puma.core.sync.dao.task.SyncTaskDao;
import com.dianping.puma.core.sync.model.BinlogInfo;
import com.dianping.puma.core.sync.model.task.CatchupTask;
import com.dianping.puma.core.sync.model.task.DumpTask;
import com.dianping.puma.core.sync.model.task.SyncTask;
import com.dianping.puma.core.sync.model.task.Task;
import com.dianping.puma.core.sync.model.task.Type;
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
<<<<<<< HEAD
        List<Long> localSyncTaskIds = binlogInfoService.findSyncTaskIds();
        // Sync tasks stored in remote MongoDB.
        List<SyncTask> remoteSyncTasks = result.asList();
        List<Long> remoteSyncTaskIds = this.findSyncTaskId(remoteSyncTasks);
=======
        List<String> localSyncTaskClientNames = binlogInfoService.findSyncTaskClientNames();
        // Sync tasks stored in remote MongoDB.
        List<SyncTask> remoteSyncTasks = result.asList();
        List<String> remoteSyncTaskClientNames = this.findSyncTaskClientNames(remoteSyncTasks);
>>>>>>> 692a38483d8b86cc391587d855ef041a5ec1aacc

        // Tasks found.
        List<SyncTask> syncTasks = new ArrayList<SyncTask>();

        // Old && Modified && New sync tasks.
        for (SyncTask remoteSyncTask: remoteSyncTasks) {
<<<<<<< HEAD
            long remoteSyncTaskId = remoteSyncTask.getSyncTaskId();

            if (localSyncTaskIds.contains(remoteSyncTaskId)) {
                if (remoteSyncTask.getExecuted()) {
                    // Old sync tasks.
                    SyncTask syncTask = this.find(Type.SYNC, remoteSyncTaskId);
                    syncTasks.add(syncTask);
=======
            String remoteSyncTaskClientName = remoteSyncTask.getPumaClientName();

            if (localSyncTaskClientNames.contains(remoteSyncTaskClientName)) {
                if (remoteSyncTask.getExecuted()) {
                    // Old sync tasks.
                    remoteSyncTask.setBinlogInfo(binlogInfoService.getBinlogInfo(remoteSyncTaskClientName));
                    syncTasks.add(remoteSyncTask);
>>>>>>> 692a38483d8b86cc391587d855ef041a5ec1aacc
                } else {
                    // @TODO
                    // Modified sync tasks.
                }
            } else {
<<<<<<< HEAD
                // New sync tasks.
                binlogInfoService.saveBinlogInfo(remoteSyncTaskId, remoteSyncTask.getBinlogInfo());
                SyncTask syncTask = this.find(Type.SYNC, remoteSyncTaskId);
                syncTasks.add(syncTask);
                // Update mongoDB.
                syncTask.setExecuted(true);
                this.syncTaskDao.save(syncTask);
=======
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
>>>>>>> 692a38483d8b86cc391587d855ef041a5ec1aacc
            }
        }

        // Removed sync tasks.
<<<<<<< HEAD
        for (Long localSyncTaskId: localSyncTaskIds) {
            if (!remoteSyncTaskIds.contains(localSyncTaskId)) {
                // Removed sync tasks.
                binlogInfoService.removeBinlogInfo(localSyncTaskId);
=======
        for (String localSyncTaskClientName: localSyncTaskClientNames) {
            if (!remoteSyncTaskClientNames.contains(localSyncTaskClientName)) {
                // Removed sync tasks.
                binlogInfoService.removeBinlogInfo(localSyncTaskClientName);
>>>>>>> 692a38483d8b86cc391587d855ef041a5ec1aacc
            }
        }

        return syncTasks;
<<<<<<< HEAD
=======
    }

    private List<String> findSyncTaskClientNames(List<SyncTask> syncTasks) {
        List<String> syncTaskClientNames = new ArrayList<String>();
        for (SyncTask syncTask: syncTasks) {
            syncTaskClientNames.add(syncTask.getPumaClientName());
        }
        return syncTaskClientNames;
>>>>>>> 692a38483d8b86cc391587d855ef041a5ec1aacc
    }

    private List<Long> findSyncTaskId(List<SyncTask> syncTasks) {
        List<Long> syncTaskIds = new ArrayList<Long>();
        for(SyncTask syncTask: syncTasks) {
            syncTaskIds.add(syncTask.getSyncTaskId());
        }
        return syncTaskIds;
    }

    // Find a task with its correct binlog info.
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Task> T find(Type type, long taskId) {
        T task = null;
        switch (type) {
        case SYNC:
            task = (T) syncTaskDao.getDatastore().getByKey(SyncTask.class,
                  new Key<SyncTask>(SyncTask.class, taskId));
<<<<<<< HEAD
=======
            task.setExecuted(true);
            this.syncTaskDao.save((SyncTask)task);

            if (task != null) {
                BinlogInfo binlogInfo = task.getBinlogInfo();
                binlogInfoService.saveBinlogInfo(((SyncTask) task).getPumaClientName(), binlogInfo);
            }

>>>>>>> 692a38483d8b86cc391587d855ef041a5ec1aacc
            break;
        case CATCHUP:
            task = (T) catchupTaskDao.getDatastore().getByKey(CatchupTask.class,
                  new Key<CatchupTask>(CatchupTask.class, taskId));
<<<<<<< HEAD
            break;
        case DUMP:
            task =  (T) dumpTaskDao.getDatastore().getByKey(DumpTask.class,
                  new Key<DumpTask>(DumpTask.class, taskId));
            break;
        }

        if (task != null) {
            BinlogInfo binlogInfo = binlogInfoService.getBinlogInfo(taskId);
            if (binlogInfo != null) {
                task.setBinlogInfo(binlogInfo);
            }
=======
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
    public void recordBinlog(String syncServerName, Type type, long taskId, BinlogInfo binlogInfo) {
        Task task = this.find(type, taskId);

        switch (type) {
        case SYNC:
            binlogInfoService.saveBinlogInfo(((SyncTask)task).getPumaClientName(), binlogInfo);
            break;
        case CATCHUP:
            binlogInfoService.saveBinlogInfo(((CatchupTask)task).getPumaClientName(), binlogInfo);
            break;
>>>>>>> 692a38483d8b86cc391587d855ef041a5ec1aacc
        }

        return task;
    }
<<<<<<< HEAD

    @Override
    public void recordBinlog(String syncServerName, Type type, long taskId, BinlogInfo binlogInfo) {
        binlogInfoService.saveBinlogInfo(taskId, binlogInfo);
    }
=======
>>>>>>> 692a38483d8b86cc391587d855ef041a5ec1aacc
}
