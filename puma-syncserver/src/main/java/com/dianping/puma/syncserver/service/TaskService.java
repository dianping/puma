package com.dianping.puma.syncserver.service;

import com.dianping.puma.core.entity.AbstractBaseSyncTask;
import com.dianping.puma.core.model.BinlogInfo;

public interface TaskService {

    /**
     * 记录task的binlog
     */
    void recordBinlog(AbstractBaseSyncTask task, BinlogInfo binlogInfo);

}
