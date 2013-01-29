package com.dianping.puma.admin.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.admin.monitor.SystemStatusContainer;
import com.dianping.puma.admin.service.DumpTaskService;
import com.dianping.puma.core.monitor.SwallowEventPulisher;
import com.dianping.puma.core.monitor.TaskEvent;
import com.dianping.puma.core.sync.dao.task.DumpTaskDao;
import com.dianping.puma.core.sync.model.BinlogInfo;
import com.dianping.puma.core.sync.model.task.DumpTask;
import com.dianping.puma.core.sync.model.task.Type;
import com.google.code.morphia.Key;

@Service
public class DumpTaskServiceImpl implements DumpTaskService {
    @Autowired
    DumpTaskDao dumpTaskDao;
    @Autowired
    SwallowEventPulisher taskEventPublisher;
    @Autowired
    SystemStatusContainer systemStatusContainer;

    @Override
    public Long create(DumpTask dumptask) {
        BinlogInfo binlogBin = new BinlogInfo();
        binlogBin.setBinlogFile("");
        binlogBin.setBinlogPosition(-1);
        dumptask.setBinlogInfo(binlogBin);

        Key<DumpTask> key = this.dumpTaskDao.save(dumptask);
        this.dumpTaskDao.getDatastore().ensureIndexes();
        Long id = (Long) key.getId();

        //更新本地状态
        systemStatusContainer.addStatus(Type.DUMP, id);

        //通知
        TaskEvent event = new TaskEvent();
        event.setTaskId(id);
        event.setType(Type.DUMP);
        event.setSyncServerName(dumptask.getSyncServerName());
        taskEventPublisher.publish(event);

        return id;
    }

    @Override
    public DumpTask find(Long id) {
        return this.dumpTaskDao.getDatastore().getByKey(DumpTask.class, new Key<DumpTask>(DumpTask.class, id));
    }

    @Override
    public void updateSyncTaskId(Long dumptaskId, Long syncTaskId) {
        DumpTask dumptask = this.find(dumptaskId);
        dumptask.setSyncTaskId(syncTaskId);
        this.dumpTaskDao.save(dumptask);
    }

}
