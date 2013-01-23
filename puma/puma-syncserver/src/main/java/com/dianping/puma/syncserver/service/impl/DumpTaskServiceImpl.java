package com.dianping.puma.syncserver.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.dao.task.DumpTaskDao;
import com.dianping.puma.core.sync.model.task.DumpTask;
import com.dianping.puma.core.sync.model.task.TaskState;
import com.dianping.puma.syncserver.service.DumpTaskService;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;

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
}
