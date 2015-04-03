package com.dianping.puma.core.dao.morphia;

import com.dianping.puma.core.dao.ShardDumpTaskDao;
import com.dianping.puma.core.dao.morphia.helper.MongoClient;
import com.dianping.puma.core.entity.ShardDumpTask;
import com.dianping.puma.core.entity.morphia.ShardDumpTaskMorphia;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("shardDumpTaskDao")
public class ShardDumpTaskMorphiaDao extends MongoBaseDao<ShardDumpTaskMorphia> implements ShardDumpTaskDao {

    @Autowired
    public ShardDumpTaskMorphiaDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }

    @Override
    public ShardDumpTask find(String name) {
        Query<ShardDumpTaskMorphia> q = this.getDatastore().createQuery(ShardDumpTaskMorphia.class);
        q.field("name").equal(name);
        ShardDumpTaskMorphia syncTaskMorphia = this.findOne(q);
        return (syncTaskMorphia == null) ? null : syncTaskMorphia.getEntity();
    }

    @Override
    public List<ShardDumpTask> findBySyncServerName(String syncServerName) {
        Query<ShardDumpTaskMorphia> q = this.getDatastore().createQuery(ShardDumpTaskMorphia.class).disableValidation();
        q.field("entity.syncServerName").equal(syncServerName);
        QueryResults<ShardDumpTaskMorphia> result = this.find(q);
        List<ShardDumpTaskMorphia> syncTaskMorphias = result.asList();

        List<ShardDumpTask> entities = new ArrayList<ShardDumpTask>();
        for (ShardDumpTaskMorphia syncTaskMorphia : syncTaskMorphias) {
            entities.add(syncTaskMorphia.getEntity());
        }
        return entities;
    }

    @Override
    public List<ShardDumpTask> findAll() {
        Query<ShardDumpTaskMorphia> q = this.getDatastore().createQuery(ShardDumpTaskMorphia.class);
        QueryResults<ShardDumpTaskMorphia> result = this.find(q);
        List<ShardDumpTaskMorphia> syncTaskMorphias = result.asList();

        List<ShardDumpTask> syncTasks = new ArrayList<ShardDumpTask>();
        for (ShardDumpTaskMorphia syncTaskMorphia : syncTaskMorphias) {
            syncTasks.add(syncTaskMorphia.getEntity());
        }
        return syncTasks;
    }

    @Override
    public void create(ShardDumpTask syncTask) {
        ShardDumpTaskMorphia syncTaskMorphia = new ShardDumpTaskMorphia(syncTask);
        this.save(syncTaskMorphia);
        this.getDatastore().ensureIndexes();
    }

    @Override
    public void remove(String name) {
        Query<ShardDumpTaskMorphia> q = this.getDatastore().createQuery(ShardDumpTaskMorphia.class);
        q.field("name").equal(name);
        this.deleteByQuery(q);
    }


    @Override
    public List<ShardDumpTask> find(int offset, int limit) {
        Query<ShardDumpTaskMorphia> q = this.getDatastore().createQuery(ShardDumpTaskMorphia.class);
        q.offset(offset);
        q.limit(limit);
        QueryResults<ShardDumpTaskMorphia> result = this.find(q);
        List<ShardDumpTask> syncTasks = new ArrayList<ShardDumpTask>();
        for (ShardDumpTaskMorphia syncTaskMorphia : result.asList()) {
            syncTasks.add(syncTaskMorphia.getEntity());
        }
        return syncTasks;
    }
}
