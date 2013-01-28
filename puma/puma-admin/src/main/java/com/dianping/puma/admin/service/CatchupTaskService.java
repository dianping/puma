package com.dianping.puma.admin.service;

import com.dianping.puma.core.sync.model.BinlogInfo;
import com.dianping.puma.core.sync.model.task.CatchupTask;

public interface CatchupTaskService {

    Long create(CatchupTask catchupTask, BinlogInfo binlogInfo);

    CatchupTask find(Long id);

}
