package com.dianping.puma.admin.service;

import org.bson.types.ObjectId;

import com.dianping.puma.core.sync.model.action.SyncTaskActionState;

public interface SyncTaskActionStateService {

    ObjectId save(SyncTaskActionState syncTaskActionState);

    SyncTaskActionState find(ObjectId objectId);

}
