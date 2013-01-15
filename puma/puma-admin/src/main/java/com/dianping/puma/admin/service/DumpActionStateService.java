package com.dianping.puma.admin.service;

import org.bson.types.ObjectId;

import com.dianping.puma.core.sync.model.action.DumpActionState;

public interface DumpActionStateService {

    ObjectId save(DumpActionState dumpActionState);

    DumpActionState find(ObjectId objectId);

}
