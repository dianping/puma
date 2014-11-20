package com.dianping.puma.admin.service.impl;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.admin.service.PumaServerConfigService;
import com.dianping.puma.core.sync.dao.config.PumaServerConfigDao;
import com.dianping.puma.core.sync.model.config.PumaServerConfig;
import com.google.code.morphia.Key;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;

@Service("pumaServerConfigService")
public class PumaServerConfigServiceImpl implements PumaServerConfigService {
    @Autowired
    PumaServerConfigDao pumaServerConfigDao;

    @Override
    public ObjectId save(PumaServerConfig pumaServerConfig) {
        Key<PumaServerConfig> key = this.pumaServerConfigDao.save(pumaServerConfig);
        this.pumaServerConfigDao.getDatastore().ensureIndexes();
        return (ObjectId) key.getId();
    }

    @Override
    public void modify(PumaServerConfig pumaServerConfig) {
        this.pumaServerConfigDao.save(pumaServerConfig);
    }

    @Override
    public void remove(ObjectId id) {
        Query<PumaServerConfig> q = pumaServerConfigDao.getDatastore().createQuery(PumaServerConfig.class);
        q.field("_id").equal(id);
        pumaServerConfigDao.deleteByQuery(q);
    }

    @Override
    public List<PumaServerConfig> findAll() {
        Query<PumaServerConfig> q = pumaServerConfigDao.getDatastore().createQuery(PumaServerConfig.class);
        QueryResults<PumaServerConfig> result = pumaServerConfigDao.find(q);
        return result.asList();
    }

    @Override
    public PumaServerConfig find(ObjectId objectId) {
        return this.pumaServerConfigDao.getDatastore().getByKey(PumaServerConfig.class,
                new Key<PumaServerConfig>(PumaServerConfig.class, objectId));
    }

    @Override
    public PumaServerConfig find(String name) {
        Query<PumaServerConfig> q = pumaServerConfigDao.getDatastore().createQuery(PumaServerConfig.class);
        q.field("mysqlName").equal(name);
        return pumaServerConfigDao.findOne(q);
    }

}
