package com.dianping.puma.admin.service;

import com.dianping.puma.core.sync.model.action.DumpAction;

public interface DumpActionService {

    /**
     * 创建DumpAction，同时创建DumpActionState
     */
    Long create(DumpAction dumpAction);

    DumpAction find(Long id);

}
