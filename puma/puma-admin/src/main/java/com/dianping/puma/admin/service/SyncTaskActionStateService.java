package com.dianping.puma.admin.service;

import java.util.Map;

import org.bson.types.ObjectId;

import com.dianping.puma.core.sync.model.action.ActionState.State;
import com.dianping.puma.core.sync.model.action.SyncTaskActionState;

public interface SyncTaskActionStateService {

    ObjectId create(SyncTaskActionState syncTaskActionState);

    SyncTaskActionState find(ObjectId objectId);

    void updateState(ObjectId id, State state, Map<String, String> params);

}
