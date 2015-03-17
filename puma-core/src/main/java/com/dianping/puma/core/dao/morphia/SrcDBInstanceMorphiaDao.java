package com.dianping.puma.core.dao.morphia;

import com.dianping.puma.core.dao.SrcDBInstanceDao;
import com.dianping.puma.core.entity.SrcDBInstance;
import com.dianping.puma.core.entity.morphia.SrcDBInstanceMorphiaEntity;
import com.google.code.morphia.dao.BasicDAO;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;
import com.google.code.morphia.query.UpdateOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("srcDBInstanceDao")
public class SrcDBInstanceMorphiaDao extends BasicDAO<SrcDBInstanceMorphiaEntity, String>
        implements SrcDBInstanceDao {

    @Autowired
    public SrcDBInstanceMorphiaDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }

    public SrcDBInstance find(String id) {
        Query<SrcDBInstanceMorphiaEntity> q = this.getDatastore().createQuery(SrcDBInstanceMorphiaEntity.class);
        q.field("id").equal(id);
        SrcDBInstanceMorphiaEntity morphiaEntity = this.findOne(q);
        return (morphiaEntity == null) ? null : morphiaEntity.getEntity();
    }

    public List<SrcDBInstance> findByIp(String ip) {
        Query<SrcDBInstanceMorphiaEntity> q = this.getDatastore().createQuery(SrcDBInstanceMorphiaEntity.class);
        q.field("entity.metaHost").equal(ip);
        QueryResults<SrcDBInstanceMorphiaEntity> result = this.find(q);
        List<SrcDBInstanceMorphiaEntity> morphiaEntities = result.asList();

        List<SrcDBInstance> entities = new ArrayList<SrcDBInstance>();
        for (SrcDBInstanceMorphiaEntity morphiaEntity : morphiaEntities) {
            entities.add(morphiaEntity.getEntity());
        }
        return entities;
    }

    public SrcDBInstance findByName(String name) {
        Query<SrcDBInstanceMorphiaEntity> q = this.getDatastore().createQuery(SrcDBInstanceMorphiaEntity.class).disableValidation();
        q.field("entity.name").equal(name);
        SrcDBInstanceMorphiaEntity morphiaEntity = this.findOne(q);
        return (morphiaEntity == null) ? null : morphiaEntity.getEntity();
    }

    public List<SrcDBInstance> findAll() {
        Query<SrcDBInstanceMorphiaEntity> q = this.getDatastore().createQuery(SrcDBInstanceMorphiaEntity.class);
        QueryResults<SrcDBInstanceMorphiaEntity> result = this.find(q);
        List<SrcDBInstanceMorphiaEntity> morphiaEntities = result.asList();

        List<SrcDBInstance> entities = new ArrayList<SrcDBInstance>();
        for (SrcDBInstanceMorphiaEntity morphiaEntity : morphiaEntities) {
            entities.add(morphiaEntity.getEntity());
        }
        return entities;
    }

    public void create(SrcDBInstance entity) {
        SrcDBInstanceMorphiaEntity morphiaEntity = new SrcDBInstanceMorphiaEntity(entity);
        this.save(morphiaEntity);
        this.getDatastore().ensureIndexes();
    }

    public void update(SrcDBInstance entity) {
        SrcDBInstanceMorphiaEntity morphiaEntity = new SrcDBInstanceMorphiaEntity(entity);
        Query<SrcDBInstanceMorphiaEntity> q = this.getDatastore().createQuery(SrcDBInstanceMorphiaEntity.class);
        q.field("id").equal(morphiaEntity.getId());
        UpdateOperations<SrcDBInstanceMorphiaEntity> uop = this.getDatastore().createUpdateOperations(SrcDBInstanceMorphiaEntity.class);
        uop.set("entity", entity);
        this.update(q, uop);
        this.getDatastore().ensureIndexes();
    }

    public void remove(String id) {
        Query<SrcDBInstanceMorphiaEntity> q = this.getDatastore().createQuery(SrcDBInstanceMorphiaEntity.class);
        q.field("id").equal(id);
        this.deleteByQuery(q);
    }
}
