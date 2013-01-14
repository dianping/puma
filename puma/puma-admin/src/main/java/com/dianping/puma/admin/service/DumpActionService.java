package com.dianping.puma.admin.service;

import org.bson.types.ObjectId;

import com.dianping.puma.core.sync.model.action.DumpAction;

public interface DumpActionService {

    /**
     * 创建DumpAction，同时创建DumpActionState
     */
    ObjectId create(DumpAction dumpAction);

    DumpAction find(ObjectId objectId);

}
