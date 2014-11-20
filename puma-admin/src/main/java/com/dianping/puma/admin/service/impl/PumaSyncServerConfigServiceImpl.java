package com.dianping.puma.admin.service.impl;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.admin.service.PumaSyncServerConfigService;
import com.dianping.puma.core.sync.dao.config.PumaSyncServerConfigDao;
import com.dianping.puma.core.sync.model.config.PumaSyncServerConfig;
import com.google.code.morphia.Key;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;

@Service("pumaSyncServerConfigService")
public class PumaSyncServerConfigServiceImpl implements PumaSyncServerConfigService {
    @Autowired
    PumaSyncServerConfigDao pumaSyncServerConfigDao;

    @Override
    public ObjectId save(PumaSyncServerConfig pumaSyncServerConfig) {
        Key<PumaSyncServerConfig> key = this.pumaSyncServerConfigDao.save(pumaSyncServerConfig);
        this.pumaSyncServerConfigDao.getDatastore().ensureIndexes();
        return (ObjectId) key.getId();
    }

    @Override
    public void modify(PumaSyncServerConfig pumaSyncServerConfig) {
        this.pumaSyncServerConfigDao.save(pumaSyncServerConfig);
    }

    @Override
    public void remove(ObjectId id) {
        Query<PumaSyncServerConfig> q = pumaSyncServerConfigDao.getDatastore().createQuery(PumaSyncServerConfig.class);
        q.field("_id").equal(id);
        pumaSyncServerConfigDao.deleteByQuery(q);
    }

    @Override
    public List<PumaSyncServerConfig> findAll() {
        Query<PumaSyncServerConfig> q = pumaSyncServerConfigDao.getDatastore().createQuery(PumaSyncServerConfig.class);
        QueryResults<PumaSyncServerConfig> result = pumaSyncServerConfigDao.find(q);
        return result.asList();
    }

    @Override
    public PumaSyncServerConfig find(ObjectId objectId) {
        return this.pumaSyncServerConfigDao.getDatastore().getByKey(PumaSyncServerConfig.class,
                new Key<PumaSyncServerConfig>(PumaSyncServerConfig.class, objectId));
    }

    @Override
    public PumaSyncServerConfig find(String name) {
        Query<PumaSyncServerConfig> q = pumaSyncServerConfigDao.getDatastore().createQuery(PumaSyncServerConfig.class);
        q.field("name").equal(name);
        return pumaSyncServerConfigDao.findOne(q);
    }

}
