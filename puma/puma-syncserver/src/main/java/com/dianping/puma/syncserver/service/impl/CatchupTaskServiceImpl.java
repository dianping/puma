package com.dianping.puma.syncserver.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.dao.task.CatchupTaskDao;
import com.dianping.puma.core.sync.model.task.CatchupTask;
import com.dianping.puma.core.sync.model.task.TaskState;
import com.dianping.puma.core.sync.model.task.TaskState.State;
import com.dianping.puma.syncserver.service.CatchupTaskService;
import com.google.code.morphia.Key;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;
import com.google.code.morphia.query.UpdateOperations;

@Service("catchupTaskService")
public class CatchupTaskServiceImpl implements CatchupTaskService {
    @Autowired
    CatchupTaskDao catchupTaskDao;

    @Override
    public List<CatchupTask> find(List<TaskState.State> states, String syncServerName) {
        Query<CatchupTask> q = catchupTaskDao.getDatastore().createQuery(CatchupTask.class);
        q.field("taskState.state").in(states);
        q.filter("syncServerName", syncServerName);
        QueryResults<CatchupTask> result = catchupTaskDao.find(q);
        return result.asList();
    }

    @Override
    public void updateState(long id, State state, Map<String, String> params) {
        UpdateOperations<CatchupTask> ops = this.catchupTaskDao.getDatastore().createUpdateOperations(CatchupTask.class)
                .set("taskState.state", state);
        if (params != null) {
            ops.set("taskState.params", params);
        }
        ops.set("taskState.detail", state.getDesc());
        ops.set("taskState.lastUpdateTime", new Date());
        this.catchupTaskDao.getDatastore().update(new Key<CatchupTask>(CatchupTask.class, id), ops);
    }
}
