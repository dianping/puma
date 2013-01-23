package com.dianping.puma.syncserver.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.dao.task.CatchupTaskDao;
import com.dianping.puma.core.sync.model.task.CatchupTask;
import com.dianping.puma.core.sync.model.task.TaskState;
import com.dianping.puma.syncserver.service.CatchupTaskService;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;

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
}
