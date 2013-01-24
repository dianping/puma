package com.dianping.puma.syncserver.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.dao.task.DumpTaskDao;
import com.dianping.puma.core.sync.model.task.DumpTask;
import com.dianping.puma.core.sync.model.task.TaskState;
import com.dianping.puma.core.sync.model.task.TaskState.State;
import com.dianping.puma.syncserver.service.DumpTaskService;
import com.google.code.morphia.Key;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;
import com.google.code.morphia.query.UpdateOperations;

@Service
public class DumpTaskServiceImpl implements DumpTaskService {
    @Autowired
    DumpTaskDao dumpTaskDao;

    @Override
    public List<DumpTask> find(List<TaskState.State> states, String syncServerName) {
        Query<DumpTask> q = dumpTaskDao.getDatastore().createQuery(DumpTask.class);
        q.field("taskState.state").in(states);
        q.filter("syncServerName", syncServerName);
        QueryResults<DumpTask> result = dumpTaskDao.find(q);
        return result.asList();
    }

    @Override
    public void updateState(long id, State state, Map<String, String> params) {
        UpdateOperations<DumpTask> ops = this.dumpTaskDao.getDatastore().createUpdateOperations(DumpTask.class)
                .set("taskState.state", state);
        if (params != null) {
            ops.set("taskState.params", params);
        }
        ops.set("taskState.detail", state.getDesc());
        ops.set("taskState.lastUpdateTime", new Date());
        this.dumpTaskDao.getDatastore().update(new Key<DumpTask>(DumpTask.class, id), ops);
    }
}
