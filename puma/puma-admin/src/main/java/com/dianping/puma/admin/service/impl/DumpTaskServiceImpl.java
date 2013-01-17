package com.dianping.puma.admin.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.admin.service.DumpActionService;
import com.dianping.puma.core.sync.dao.task.DumpTaskDao;
import com.dianping.puma.core.sync.model.BinlogInfo;
import com.dianping.puma.core.sync.model.task.DumpTask;
import com.dianping.puma.core.sync.model.task.TaskState;
import com.dianping.puma.core.sync.model.task.TaskState.State;
import com.google.code.morphia.Key;
import com.google.code.morphia.query.UpdateOperations;

@Service("dumpActionService")
public class DumpTaskServiceImpl implements DumpActionService {
    @Autowired
    DumpTaskDao dumpTaskDao;

    @Override
    public Long create(DumpTask dumpAction) {
        //创建DumpActionState
        TaskState actionState = new TaskState();
        actionState.setState(State.PREPARABLE);
        actionState.setDetail(State.PREPARABLE.getDesc());
        Date curDate = new Date();
        actionState.setCreateTime(curDate);
        actionState.setLastUpdateTime(curDate);
        BinlogInfo binlogBin = new BinlogInfo();
        binlogBin.setBinlogFile("");
        binlogBin.setBinlogPosition(-1);
        actionState.setBinlogInfo(binlogBin);
        dumpAction.setTaskState(actionState);

        Key<DumpTask> key = this.dumpTaskDao.save(dumpAction);
        this.dumpTaskDao.getDatastore().ensureIndexes();
        Long id = (Long) key.getId();

        return id;
    }

    @Override
    public DumpTask find(Long id) {
        return this.dumpTaskDao.getDatastore().getByKey(DumpTask.class, new Key<DumpTask>(DumpTask.class, id));
    }

    @Override
    public void updateSyncTaskId(Long dumpActionId, Long syncTaskId) {
        UpdateOperations<DumpTask> ops = this.dumpTaskDao.getDatastore().createUpdateOperations(DumpTask.class)
                .set("syncTaskId", syncTaskId);
        this.dumpTaskDao.getDatastore().update(new Key<DumpTask>(DumpTask.class, dumpActionId), ops);
    }

}
