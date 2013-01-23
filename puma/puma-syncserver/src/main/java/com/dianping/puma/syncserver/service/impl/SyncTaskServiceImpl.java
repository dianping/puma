package com.dianping.puma.syncserver.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.dao.task.SyncTaskDao;
import com.dianping.puma.core.sync.model.task.SyncTask;
import com.dianping.puma.core.sync.model.task.TaskState;
import com.dianping.puma.syncserver.service.SyncTaskService;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;

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

}
