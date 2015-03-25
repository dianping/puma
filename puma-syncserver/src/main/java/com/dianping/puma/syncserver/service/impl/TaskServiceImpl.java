package com.dianping.puma.syncserver.service.impl;

import com.dianping.puma.core.entity.AbstractBaseSyncTask;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.syncserver.service.BinlogInfoService;
import org.springframework.stereotype.Service;

import com.dianping.puma.syncserver.service.TaskService;

@Service
public class TaskServiceImpl implements TaskService {

    BinlogInfoService binlogInfoService;

    @Override
    public void recordBinlog(AbstractBaseSyncTask abstractTask, BinlogInfo binlogInfo) {
        binlogInfoService.saveBinlogInfo(abstractTask.getPumaClientName(), binlogInfo);
    }
}