package com.dianping.puma.admin.service.impl;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.admin.service.SyncTaskActionStateService;
import com.dianping.puma.core.sync.dao.action.SyncTaskActionStateDao;
import com.dianping.puma.core.sync.model.action.SyncTaskActionState;
import com.google.code.morphia.Key;

@Service("syncTaskActionStateService")
public class SyncTaskActionStateServiceImpl implements SyncTaskActionStateService {
    @Autowired
    SyncTaskActionStateDao syncTaskActionStateDao;

    @Override
    public ObjectId save(SyncTaskActionState syncTaskActionState) {
        Key<SyncTaskActionState> key = this.syncTaskActionStateDao.save(syncTaskActionState);
        this.syncTaskActionStateDao.getDatastore().ensureIndexes();
        return (ObjectId) key.getId();
    }

    @Override
    public SyncTaskActionState find(ObjectId objectId) {
        return this.syncTaskActionStateDao.getDatastore().getByKey(SyncTaskActionState.class,
                new Key<SyncTaskActionState>(SyncTaskActionState.class, objectId));
    }

}
