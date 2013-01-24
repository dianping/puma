package com.dianping.puma.syncserver.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.dao.task.SyncTaskDao;
import com.dianping.puma.core.sync.model.task.SyncTask;
import com.dianping.puma.core.sync.model.task.TaskState;
import com.dianping.puma.core.sync.model.task.TaskState.State;
import com.dianping.puma.syncserver.service.SyncTaskService;
import com.google.code.morphia.Key;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;
import com.google.code.morphia.query.UpdateOperations;

@Service
public class SyncTaskServiceImpl implements SyncTaskService {
    @Autowired
    SyncTaskDao syncTaskDao;

    @Override
    public List<SyncTask> find(List<TaskState.State> states, String syncServerName) {
        Query<SyncTask> q = syncTaskDao.getDatastore().createQuery(SyncTask.class);
        q.field("taskState.state").in(states);
        q.filter("syncServerName", syncServerName);
        QueryResults<SyncTask> result = syncTaskDao.find(q);
        return result.asList();
    }

    @Override
    public void updateState(long id, State state, Map<String, String> params) {
        UpdateOperations<SyncTask> ops = this.syncTaskDao.getDatastore().createUpdateOperations(SyncTask.class)
                .set("taskState.state", state);
        if (params != null) {
            ops.set("taskState.params", params);
        }
        ops.set("taskState.detail", state.getDesc());
        ops.set("taskState.lastUpdateTime", new Date());
        this.syncTaskDao.getDatastore().update(new Key<SyncTask>(SyncTask.class, id), ops);
    }

}
