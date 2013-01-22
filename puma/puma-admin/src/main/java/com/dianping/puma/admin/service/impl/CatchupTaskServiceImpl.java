package com.dianping.puma.admin.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.admin.service.CatchupTaskService;
import com.dianping.puma.core.sync.dao.task.CatchupTaskDao;
import com.dianping.puma.core.sync.model.task.CatchupTask;
import com.dianping.puma.core.sync.model.task.TaskState;
import com.dianping.puma.core.sync.model.task.TaskState.State;
import com.google.code.morphia.Key;

@Service("catchupTaskService")
public class CatchupTaskServiceImpl implements CatchupTaskService {
    @Autowired
    CatchupTaskDao catchupTaskDao;

    @Override
    public Long create(CatchupTask catchupTask) {
        //验证仅有一个databaseConfig
        if (catchupTask.getMysqlMapping().getDatabases() == null || catchupTask.getMysqlMapping().getDatabases().size() == 0
                || catchupTask.getMysqlMapping().getDatabases().size() > 1) {
            throw new IllegalArgumentException("创建失败，<database>配置必须有且仅能有一个！");
        }
        //验证table
        if (catchupTask.getMysqlMapping().getDatabases().get(0).getTables() == null
                || catchupTask.getMysqlMapping().getDatabases().get(0).getTables().size() == 0) {
            throw new IllegalArgumentException("创建失败，<table>配置必须至少有一个！");
        }
        //创建SyncTaskState
        TaskState taskState = new TaskState();
        taskState.setState(State.PREPARABLE);
        taskState.setDetail(State.PREPARABLE.getDesc());
        Date curDate = new Date();
        taskState.setCreateTime(curDate);
        taskState.setLastUpdateTime(curDate);
        catchupTask.setTaskState(taskState);
        //开始保存
        Key<CatchupTask> key = this.catchupTaskDao.save(catchupTask);
        this.catchupTaskDao.getDatastore().ensureIndexes();
        Long id = (Long) key.getId();

        return id;
    }

    @Override
    public CatchupTask find(Long id) {
        return this.catchupTaskDao.getDatastore().getByKey(CatchupTask.class, new Key<CatchupTask>(CatchupTask.class, id));
    }

}
