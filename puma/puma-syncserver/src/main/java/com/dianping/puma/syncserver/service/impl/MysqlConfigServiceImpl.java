package com.dianping.puma.syncserver.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.sync.dao.config.MysqlConfigDao;
import com.dianping.puma.core.sync.model.config.MysqlConfig;
import com.dianping.puma.core.sync.model.config.MysqlHost;
import com.dianping.puma.syncserver.service.MysqlConfigService;
import com.google.code.morphia.Key;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;

@Service("mysqlConfigService")
public class MysqlConfigServiceImpl implements MysqlConfigService {
    @Autowired
    MysqlConfigDao mysqlConfigDao;

    @Override
    public ObjectId save(MysqlConfig mysqlConfig) {
        //serverId不能重复
        List<MysqlHost> mysqlhosts = mysqlConfig.getHosts();
        Set<Long> serverIds = new HashSet<Long>();
        for (MysqlHost host : mysqlhosts) {
            long sid = host.getServerId();
            if (!serverIds.add(sid)) {
                throw new IllegalArgumentException("serverId不能重复");
            }
        }

        Key<MysqlConfig> key = this.mysqlConfigDao.save(mysqlConfig);
        this.mysqlConfigDao.getDatastore().ensureIndexes();
        return (ObjectId) key.getId();
    }

    @Override
    public void modify(MysqlConfig mysqlConfig) {
        //serverId不能重复
        List<MysqlHost> mysqlhosts = mysqlConfig.getHosts();
        Set<Long> serverIds = new HashSet<Long>();
        for (MysqlHost host : mysqlhosts) {
            long sid = host.getServerId();
            if (!serverIds.add(sid)) {
                throw new IllegalArgumentException("serverId不能重复");
            }
            if (sid < 0) {
                throw new IllegalArgumentException("serverId必须大于0");
            }
        }
        this.mysqlConfigDao.save(mysqlConfig);
    }

    @Override
    public void remove(ObjectId id) {
        Query<MysqlConfig> q = mysqlConfigDao.getDatastore().createQuery(MysqlConfig.class);
        q.field("_id").equal(id);
        mysqlConfigDao.deleteByQuery(q);
    }

    @Override
    public List<MysqlConfig> findAll() {
        Query<MysqlConfig> q = mysqlConfigDao.getDatastore().createQuery(MysqlConfig.class);
        QueryResults<MysqlConfig> result = mysqlConfigDao.find(q);
        return result.asList();
    }

    @Override
    public MysqlConfig find(ObjectId objectId) {
        return this.mysqlConfigDao.getDatastore().getByKey(MysqlConfig.class, new Key<MysqlConfig>(MysqlConfig.class, objectId));
    }

    @Override
    public MysqlConfig find(String name) {
        Query<MysqlConfig> q = mysqlConfigDao.getDatastore().createQuery(MysqlConfig.class);
        q.field("name").equal(name);
        return mysqlConfigDao.findOne(q);
    }

}
