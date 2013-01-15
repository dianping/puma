package com.dianping.puma.admin.service.impl;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.admin.service.SyncTaskActionService;
import com.dianping.puma.admin.service.SyncTaskActionStateService;
import com.dianping.puma.core.sync.dao.action.SyncTaskActionDao;
import com.dianping.puma.core.sync.model.action.ActionState.State;
import com.dianping.puma.core.sync.model.action.SyncTaskAction;
import com.dianping.puma.core.sync.model.action.SyncTaskActionState;
import com.dianping.puma.core.sync.model.mapping.DatabaseMapping;
import com.dianping.puma.core.sync.model.mapping.MysqlMapping;
import com.google.code.morphia.Key;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;

@Service("syncTaskActionService")
public class SyncTaskActionServiceImpl implements SyncTaskActionService {
    @Autowired
    SyncTaskActionDao syncTaskActionDao;
    @Autowired
    SyncTaskActionStateService syncTaskActionStateService;

    @Override
    public ObjectId create(SyncTaskAction syncTaskAction) {
        //验证
        if (this.existsBySrcAndDest(syncTaskAction.getSrcMysqlName(), syncTaskAction.getDestMysqlName())) {
            throw new IllegalArgumentException("创建失败，已有相同的配置存在。(srcMysqlName=" + syncTaskAction.getSrcMysqlName()
                    + ", destMysqlName=" + syncTaskAction.getDestMysqlName() + ")");
        }
        //验证仅有一个databaseConfig
        if (syncTaskAction.getMysqlMapping().getDatabases() == null || syncTaskAction.getMysqlMapping().getDatabases().size() == 0
                || syncTaskAction.getMysqlMapping().getDatabases().size() > 1) {
            throw new IllegalArgumentException("创建失败，<database>配置必须有且仅能有一个！");
        }
        //验证table
        if (syncTaskAction.getMysqlMapping().getDatabases().get(0).getTables() == null
                || syncTaskAction.getMysqlMapping().getDatabases().get(0).getTables().size() == 0) {
            throw new IllegalArgumentException("创建失败，<table>配置必须至少有一个！");
        }
        //开始保存
        Key<SyncTaskAction> key = this.syncTaskActionDao.save(syncTaskAction);
        this.syncTaskActionDao.getDatastore().ensureIndexes();
        ObjectId id = (ObjectId) key.getId();
        //创建SyncTaskActionState
        SyncTaskActionState state = new SyncTaskActionState();
        state.setId(id);
        state.setState(State.PREPARABLE);
        state.setDetail(State.PREPARABLE.getDesc());
        Date curDate = new Date();
        state.setCreateTime(curDate);
        state.setLastUpdateTime(curDate);
        state.setBinlogInfo(syncTaskAction.getBinlogInfo());
        syncTaskActionStateService.create(state);

        return id;
    }

    @Override
    public boolean existsBySrcAndDest(String srcMysqlName, String destMysqlName) {
        Query<SyncTaskAction> q = syncTaskActionDao.getDatastore().createQuery(SyncTaskAction.class);
        q.field("srcMysqlName").equal(srcMysqlName);
        q.field("destMysqlName").equal(destMysqlName);
        return syncTaskActionDao.exists(q);
    }

    @Override
    public SyncTaskAction find(ObjectId objectId) {
        return this.syncTaskActionDao.getDatastore().getByKey(SyncTaskAction.class,
                new Key<SyncTaskAction>(SyncTaskAction.class, objectId));
    }

    @Override
    public List<SyncTaskAction> find(int offset, int limit) {
        Query<SyncTaskAction> q = syncTaskActionDao.getDatastore().createQuery(SyncTaskAction.class);
        q.offset(offset);
        q.limit(limit);
        QueryResults<SyncTaskAction> result = syncTaskActionDao.find(q);
        return result.asList();
    }

    /**
     * 对比新旧sync，求出新增的database或table配置(table也属于database下，故返回的都是database)<br>
     * 同时做验证：只允许新增database或table配置
     */
    @Override
    public MysqlMapping compare(MysqlMapping oldMysqlMapping, MysqlMapping newMysqlMapping) {
        return oldMysqlMapping.compare(newMysqlMapping);
    }

}
