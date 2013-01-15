package com.dianping.puma.admin.service;

import java.util.List;

import org.bson.types.ObjectId;

import com.dianping.puma.core.sync.model.action.SyncTaskAction;

public interface SyncTaskActionService {

    /**
     * 创建SyncTaskAction，同时创建SyncTaskActionState
     */
    ObjectId create(SyncTaskAction syncTaskAction);

    SyncTaskAction find(ObjectId objectId);

    List<SyncTaskAction> find(int offset, int limit);

    boolean existsBySrcAndDest(String srcMysqlName, String destMysqlName);

}
