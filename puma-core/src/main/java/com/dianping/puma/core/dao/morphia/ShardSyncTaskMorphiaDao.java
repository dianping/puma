package com.dianping.puma.core.dao.morphia;

import com.dianping.puma.core.dao.ShardSyncTaskDao;
import com.dianping.puma.core.dao.morphia.helper.MongoClient;
import com.dianping.puma.core.entity.ShardSyncTask;
import com.dianping.puma.core.entity.morphia.ShardSyncTaskMorphia;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("shardSyncTaskDao")
public class ShardSyncTaskMorphiaDao extends MongoBaseDao<ShardSyncTaskMorphia> implements ShardSyncTaskDao {

    @Autowired
    public ShardSyncTaskMorphiaDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }

    @Override
    public ShardSyncTask find(String name) {
        Query<ShardSyncTaskMorphia> q = this.getDatastore().createQuery(ShardSyncTaskMorphia.class);
        q.field("name").equal(name);
        ShardSyncTaskMorphia syncTaskMorphia = this.findOne(q);
        return (syncTaskMorphia == null) ? null : syncTaskMorphia.getEntity();
    }

    @Override
    public List<ShardSyncTask> findBySyncServerName(String syncServerName) {
        Query<ShardSyncTaskMorphia> q = this.getDatastore().createQuery(ShardSyncTaskMorphia.class).disableValidation();
        q.field("entity.syncServerName").equal(syncServerName);
        QueryResults<ShardSyncTaskMorphia> result = this.find(q);
        List<ShardSyncTaskMorphia> syncTaskMorphias = result.asList();

        List<ShardSyncTask> entities = new ArrayList<ShardSyncTask>();
        for (ShardSyncTaskMorphia syncTaskMorphia : syncTaskMorphias) {
            entities.add(syncTaskMorphia.getEntity());
        }
        return entities;
    }

    @Override
    public List<ShardSyncTask> findAll() {
        Query<ShardSyncTaskMorphia> q = this.getDatastore().createQuery(ShardSyncTaskMorphia.class);
        QueryResults<ShardSyncTaskMorphia> result = this.find(q);
        List<ShardSyncTaskMorphia> syncTaskMorphias = result.asList();

        List<ShardSyncTask> syncTasks = new ArrayList<ShardSyncTask>();
        for (ShardSyncTaskMorphia syncTaskMorphia : syncTaskMorphias) {
            syncTasks.add(syncTaskMorphia.getEntity());
        }
        return syncTasks;
    }

    @Override
    public void create(ShardSyncTask syncTask) {
        ShardSyncTaskMorphia syncTaskMorphia = new ShardSyncTaskMorphia(syncTask);
        this.save(syncTaskMorphia);
        this.getDatastore().ensureIndexes();
    }

    @Override
    public void remove(String name) {
        Query<ShardSyncTaskMorphia> q = this.getDatastore().createQuery(ShardSyncTaskMorphia.class);
        q.field("name").equal(name);
        this.deleteByQuery(q);
    }


    @Override
    public List<ShardSyncTask> find(int offset, int limit) {
        Query<ShardSyncTaskMorphia> q = this.getDatastore().createQuery(ShardSyncTaskMorphia.class);
        q.offset(offset);
        q.limit(limit);
        QueryResults<ShardSyncTaskMorphia> result = this.find(q);
        List<ShardSyncTask> syncTasks = new ArrayList<ShardSyncTask>();
        for (ShardSyncTaskMorphia syncTaskMorphia : result.asList()) {
            syncTasks.add(syncTaskMorphia.getEntity());
        }
        return syncTasks;
    }
}
