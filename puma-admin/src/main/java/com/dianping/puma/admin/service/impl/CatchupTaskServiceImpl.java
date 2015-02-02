package com.dianping.puma.admin.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.admin.monitor.SystemStatusContainer;
import com.dianping.puma.admin.service.CatchupTaskService;
import com.dianping.puma.core.monitor.SwallowEventPublisher;
import com.dianping.puma.core.monitor.TaskEvent;
import com.dianping.puma.core.sync.dao.task.CatchupTaskDao;
import com.dianping.puma.core.sync.model.BinlogInfo;
import com.dianping.puma.core.sync.model.task.CatchupTask;
import com.dianping.puma.core.sync.model.task.Type;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;
import com.google.code.morphia.Key;

@Service("catchupTaskService")
public class CatchupTaskServiceImpl implements CatchupTaskService {
    @Autowired
    CatchupTaskDao catchupTaskDao;
    @Autowired
    SwallowEventPublisher taskEventPublisher;
    @Autowired
    SystemStatusContainer systemStatusContainer;

    @Override
    public Long create(CatchupTask catchupTask, BinlogInfo binlogInfo) {
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
        catchupTask.setBinlogInfo(binlogInfo);
        //开始保存
        Key<CatchupTask> key = this.catchupTaskDao.save(catchupTask);
        this.catchupTaskDao.getDatastore().ensureIndexes();
        Long id = (Long) key.getId();

        //通知
        TaskEvent event = new TaskEvent();
        event.setTaskId(id);
        event.setType(Type.CATCHUP);
        event.setSyncServerName(catchupTask.getSyncServerName());
        try {
            taskEventPublisher.publish(event);
        } catch (SendFailedException e) {
            throw new RuntimeException("已经创建任务，但给SyncServer发送通知失败，您需要重新创建任务。");
        }

        //更新本地状态
        systemStatusContainer.addStatus(Type.CATCHUP, id);

        return id;
    }

    @Override
    public CatchupTask find(Long id) {
        return this.catchupTaskDao.getDatastore().getByKey(CatchupTask.class, new Key<CatchupTask>(CatchupTask.class, id));
    }

}
