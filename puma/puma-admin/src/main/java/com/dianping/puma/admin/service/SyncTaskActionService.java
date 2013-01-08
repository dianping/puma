package com.dianping.puma.admin.service;

import org.bson.types.ObjectId;

import com.dianping.puma.core.sync.model.action.SyncTaskAction;

public interface SyncTaskActionService {

    /**
     * 创建SyncTaskAction，同时创建SyncTaskActionState
     */
    ObjectId create(SyncTaskAction syncTaskAction);

    SyncTaskAction find(ObjectId objectId);

}
