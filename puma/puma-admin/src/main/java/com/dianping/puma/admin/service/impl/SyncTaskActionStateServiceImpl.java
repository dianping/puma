package com.dianping.puma.admin.service.impl;

import java.util.Date;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.admin.service.SyncTaskActionStateService;
import com.dianping.puma.core.sync.dao.action.SyncTaskActionStateDao;
import com.dianping.puma.core.sync.model.action.ActionState.State;
import com.dianping.puma.core.sync.model.action.SyncTaskActionState;
import com.google.code.morphia.Key;
import com.google.code.morphia.query.UpdateOperations;

@Service("syncTaskActionStateService")
public class SyncTaskActionStateServiceImpl implements SyncTaskActionStateService {
    @Autowired
    SyncTaskActionStateDao syncTaskActionStateDao;

    @Override
    public ObjectId create(SyncTaskActionState syncTaskActionState) {
        Key<SyncTaskActionState> key = this.syncTaskActionStateDao.save(syncTaskActionState);
        this.syncTaskActionStateDao.getDatastore().ensureIndexes();
        return (ObjectId) key.getId();
    }

    @Override
    public void updateState(ObjectId id, State state, Map<String, String> params) {
        UpdateOperations<SyncTaskActionState> ops = this.syncTaskActionStateDao.getDatastore()
                .createUpdateOperations(SyncTaskActionState.class).set("state", state);
        if (params != null) {
            ops.set("params", params);
        }
        ops.set("detail", state.getDesc());
        ops.set("lastUpdateTime", new Date());
        this.syncTaskActionStateDao.getDatastore().update(new Key<SyncTaskActionState>(SyncTaskActionState.class, id), ops);
    }

    @Override
    public SyncTaskActionState find(ObjectId objectId) {
        return this.syncTaskActionStateDao.getDatastore().getByKey(SyncTaskActionState.class,
                new Key<SyncTaskActionState>(SyncTaskActionState.class, objectId));
    }

}
