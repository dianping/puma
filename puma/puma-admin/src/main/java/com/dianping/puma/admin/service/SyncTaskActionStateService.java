package com.dianping.puma.admin.service;

import java.util.Map;

import org.bson.types.ObjectId;

import com.dianping.puma.core.sync.model.action.ActionState.State;
import com.dianping.puma.core.sync.model.action.SyncTaskActionState;

public interface SyncTaskActionStateService {

    Long create(SyncTaskActionState syncTaskActionState);

    SyncTaskActionState find(Long objectId);

    void updateState(Long id, State state, Map<String, String> params);

}
