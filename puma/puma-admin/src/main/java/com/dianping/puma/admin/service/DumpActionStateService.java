package com.dianping.puma.admin.service;

import com.dianping.puma.core.sync.model.action.DumpActionState;

public interface DumpActionStateService {

    Long save(DumpActionState dumpActionState);

    DumpActionState find(Long objectId);

}
