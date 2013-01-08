package com.dianping.puma.admin.service.impl;

import java.util.Date;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.admin.service.SyncTaskActionService;
import com.dianping.puma.admin.service.SyncTaskActionStateService;
import com.dianping.puma.core.sync.dao.action.SyncTaskActionDao;
import com.dianping.puma.core.sync.model.action.ActionState.State;
import com.dianping.puma.core.sync.model.action.SyncTaskAction;
import com.dianping.puma.core.sync.model.action.SyncTaskActionState;
import com.google.code.morphia.Key;

@Service("syncTaskActionService")
public class SyncTaskActionServiceImpl implements SyncTaskActionService {
    @Autowired
    SyncTaskActionDao syncTaskActionDao;
    @Autowired
    SyncTaskActionStateService syncTaskActionStateService;

    @Override
    public ObjectId create(SyncTaskAction syncTaskAction) {
        Key<SyncTaskAction> key = this.syncTaskActionDao.save(syncTaskAction);
        this.syncTaskActionDao.getDatastore().ensureIndexes();
        ObjectId id = (ObjectId) key.getId();
        //创建SyncTaskActionState
        SyncTaskActionState state = new SyncTaskActionState();
        state.setId(id);
        state.setState(State.CREATED);
        state.setDetail("创建。");
        state.setCreateTime(new Date());
        state.setBinlogInfo(syncTaskAction.getBinlogInfo());
        syncTaskActionStateService.save(state);

        return id;
    }

    @Override
    public SyncTaskAction find(ObjectId objectId) {
        return this.syncTaskActionDao.getDatastore().getByKey(SyncTaskAction.class,
                new Key<SyncTaskAction>(SyncTaskAction.class, objectId));
    }

}
