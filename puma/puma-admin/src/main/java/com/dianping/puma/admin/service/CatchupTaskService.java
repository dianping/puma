package com.dianping.puma.admin.service;

import com.dianping.puma.core.sync.model.task.CatchupTask;

public interface CatchupTaskService {

    Long create(CatchupTask catchupTask);

    CatchupTask find(Long id);

    //    List<CatchupTask> find(int offset, int limit);
    //
    //    void updateState(Long id, State state, Map<String, String> params);

}
