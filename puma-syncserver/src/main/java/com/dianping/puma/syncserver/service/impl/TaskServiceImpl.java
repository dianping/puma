package com.dianping.puma.syncserver.service.impl;

import com.dianping.puma.core.entity.AbstractBaseSyncTask;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.syncserver.service.BinlogInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.dao.task.CatchupTaskDao;
import com.dianping.puma.core.sync.dao.task.DumpTaskDao;
import com.dianping.puma.core.sync.dao.task.SyncTaskDao;
import com.dianping.puma.syncserver.service.TaskService;

@Service
public class TaskServiceImpl implements TaskService {
    @Autowired
    SyncTaskDao syncTaskDao;
    @Autowired
    CatchupTaskDao catchupTaskDao;
    @Autowired
    DumpTaskDao dumpTaskDao;
    @Autowired
    BinlogInfoService binlogInfoService;

    @Override
    public void recordBinlog(AbstractBaseSyncTask abstractTask, BinlogInfo binlogInfo) {
        binlogInfoService.saveBinlogInfo(abstractTask.getPumaClientName(), binlogInfo);
    }
}