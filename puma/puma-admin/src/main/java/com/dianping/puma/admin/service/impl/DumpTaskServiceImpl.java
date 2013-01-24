package com.dianping.puma.admin.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.admin.service.DumpTaskService;
import com.dianping.puma.core.sync.dao.task.DumpTaskDao;
import com.dianping.puma.core.sync.model.BinlogInfo;
import com.dianping.puma.core.sync.model.task.DumpTask;
import com.dianping.puma.core.sync.model.task.TaskState;
import com.dianping.puma.core.sync.model.task.TaskState.State;
import com.google.code.morphia.Key;
import com.google.code.morphia.query.UpdateOperations;

@Service
public class DumpTaskServiceImpl implements DumpTaskService {
    @Autowired
    DumpTaskDao dumpTaskDao;

    @Override
    public Long create(DumpTask dumptask) {
        //创建DumptaskState
        TaskState taskState = new TaskState();
        taskState.setState(State.RUNNABLE);
        taskState.setDetail(State.RUNNABLE.getDesc());
        Date curDate = new Date();
        taskState.setCreateTime(curDate);
        taskState.setLastUpdateTime(curDate);
        BinlogInfo binlogBin = new BinlogInfo();
        binlogBin.setBinlogFile("");
        binlogBin.setBinlogPosition(-1);
        taskState.setBinlogInfo(binlogBin);
        dumptask.setTaskState(taskState);

        Key<DumpTask> key = this.dumpTaskDao.save(dumptask);
        this.dumpTaskDao.getDatastore().ensureIndexes();
        Long id = (Long) key.getId();

        return id;
    }

    @Override
    public DumpTask find(Long id) {
        return this.dumpTaskDao.getDatastore().getByKey(DumpTask.class, new Key<DumpTask>(DumpTask.class, id));
    }

    @Override
    public void updateSyncTaskId(Long dumptaskId, Long syncTaskId) {
        UpdateOperations<DumpTask> ops = this.dumpTaskDao.getDatastore().createUpdateOperations(DumpTask.class)
                .set("syncTaskId", syncTaskId);
        this.dumpTaskDao.getDatastore().update(new Key<DumpTask>(DumpTask.class, dumptaskId), ops);
    }

}
