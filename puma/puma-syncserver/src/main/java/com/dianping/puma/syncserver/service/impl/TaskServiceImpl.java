package com.dianping.puma.syncserver.service.impl;

import java.util.List;

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
import com.google.code.morphia.query.UpdateOperations;

@Service
public class TaskServiceImpl implements TaskService {
    @Autowired
    SyncTaskDao syncTaskDao;
    @Autowired
    CatchupTaskDao catchupTaskDao;
    @Autowired
    DumpTaskDao dumpTaskDao;

    @Override
    public List<SyncTask> findSyncTasks(String syncServerName) {
        Query<SyncTask> q = syncTaskDao.getDatastore().createQuery(SyncTask.class);
        q.filter("syncServerName", syncServerName);
        QueryResults<SyncTask> result = syncTaskDao.find(q);
        return result.asList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Task> T find(Type type, long taskId) {
        switch (type) {
            case SYNC:
                return (T) syncTaskDao.getDatastore().getByKey(SyncTask.class, new Key<SyncTask>(SyncTask.class, taskId));
            case CATCHUP:
                return (T) catchupTaskDao.getDatastore().getByKey(CatchupTask.class,
                        new Key<CatchupTask>(CatchupTask.class, taskId));
            case DUMP:
                return (T) dumpTaskDao.getDatastore().getByKey(DumpTask.class, new Key<DumpTask>(DumpTask.class, taskId));
        }
        return null;
    }

    @Override
    public void recordBinlog(Type type, long taskId, BinlogInfo binlogInfo) {
        switch (type) {
            case SYNC:
                UpdateOperations<SyncTask> ops1 = this.syncTaskDao.getDatastore().createUpdateOperations(SyncTask.class)
                        .set("binlogInfo", binlogInfo);
                this.syncTaskDao.getDatastore().update(new Key<SyncTask>(SyncTask.class, taskId), ops1);
            case CATCHUP:
                UpdateOperations<CatchupTask> ops2 = this.catchupTaskDao.getDatastore().createUpdateOperations(CatchupTask.class)
                        .set("binlogInfo", binlogInfo);
                this.catchupTaskDao.getDatastore().update(new Key<CatchupTask>(CatchupTask.class, taskId), ops2);
            case DUMP:
                UpdateOperations<DumpTask> ops3 = this.dumpTaskDao.getDatastore().createUpdateOperations(DumpTask.class)
                        .set("binlogInfo", binlogInfo);
                this.dumpTaskDao.getDatastore().update(new Key<DumpTask>(DumpTask.class, taskId), ops3);
        }
    }

}
