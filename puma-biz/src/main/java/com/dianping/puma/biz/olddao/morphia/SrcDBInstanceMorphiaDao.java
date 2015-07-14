package com.dianping.puma.biz.olddao.morphia;

import com.dianping.puma.biz.olddao.SrcDBInstanceDao;
import com.dianping.puma.biz.olddao.morphia.helper.MongoClient;
import com.dianping.puma.biz.entity.old.SrcDBInstance;
import com.dianping.puma.biz.entity.morphia.SrcDBInstanceMorphia;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;
import com.google.code.morphia.query.UpdateOperations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

public class SrcDBInstanceMorphiaDao extends MongoBaseDao<SrcDBInstanceMorphia>
		implements SrcDBInstanceDao {

    @Autowired
    public SrcDBInstanceMorphiaDao(MongoClient mongoClient) {
        super(mongoClient.getDatastore());
    }


    public SrcDBInstance find(long id) {
		Query<SrcDBInstanceMorphia> q = this.getDatastore().createQuery(SrcDBInstanceMorphia.class);
		q.field("_id").equal(id);
		SrcDBInstanceMorphia srcDBInstanceMorphia = this.findOne(q);
		return (srcDBInstanceMorphia == null) ? null : srcDBInstanceMorphia.getEntity();
	}
    
	public SrcDBInstance find(String name) {
		Query<SrcDBInstanceMorphia> q = this.getDatastore().createQuery(SrcDBInstanceMorphia.class);
		q.field("name").equal(name);
		SrcDBInstanceMorphia srcDBInstanceMorphia = this.findOne(q);
		return (srcDBInstanceMorphia == null) ? null : srcDBInstanceMorphia.getEntity();
	}

	public List<SrcDBInstance> findAll() {
		Query<SrcDBInstanceMorphia> q = this.getDatastore().createQuery(SrcDBInstanceMorphia.class);
		QueryResults<SrcDBInstanceMorphia> result = this.find(q);
		List<SrcDBInstanceMorphia> srcDBInstanceMorphias = result.asList();

		List<SrcDBInstance> entities = new ArrayList<SrcDBInstance>();
		for(SrcDBInstanceMorphia srcDBInstanceMorphia: srcDBInstanceMorphias) {
			entities.add(srcDBInstanceMorphia.getEntity());
		}
		return entities;
	}

	public long count() {
		Query<SrcDBInstanceMorphia> q = this.getDatastore().createQuery(SrcDBInstanceMorphia.class);
		return this.count(q);
	}

	public List<SrcDBInstance> findByPage(int page, int pageSize) {
		Query<SrcDBInstanceMorphia> q = this.getDatastore().createQuery(SrcDBInstanceMorphia.class);
		q.offset((page - 1) * pageSize);
		q.limit(pageSize);
		QueryResults<SrcDBInstanceMorphia> result = this.find(q);
		List<SrcDBInstanceMorphia> srcDBInstanceMorphias = result.asList();

		List<SrcDBInstance> entities = new ArrayList<SrcDBInstance>();
		for (SrcDBInstanceMorphia srcDBInstanceMorphia : srcDBInstanceMorphias) {
			entities.add(srcDBInstanceMorphia.getEntity());
		}
		return entities;
	}
	
	public void create(SrcDBInstance srcDBInstance) {
		SrcDBInstanceMorphia srcDBInstanceMorphia = new SrcDBInstanceMorphia(srcDBInstance);
		this.save(srcDBInstanceMorphia);
		this.getDatastore().ensureIndexes();
	}

	public void update(SrcDBInstance srcDBInstance) {
		SrcDBInstanceMorphia srcDBInstanceMorphia = new SrcDBInstanceMorphia(srcDBInstance);
		Query<SrcDBInstanceMorphia> q = this.getDatastore().createQuery(SrcDBInstanceMorphia.class);
		q.field("name").equal(srcDBInstanceMorphia.getName());
		UpdateOperations<SrcDBInstanceMorphia> uop = this.getDatastore().createUpdateOperations(SrcDBInstanceMorphia.class);
		uop.set("entity", srcDBInstance);
		this.update(q, uop);
		this.getDatastore().ensureIndexes();
	}

	public void remove(String name) {
		Query<SrcDBInstanceMorphia> q = this.getDatastore().createQuery(SrcDBInstanceMorphia.class);
		q.field("name").equal(name);
		this.deleteByQuery(q);
	}
	
	public void remove(long id) {
		Query<SrcDBInstanceMorphia> q = this.getDatastore().createQuery(SrcDBInstanceMorphia.class);
		q.field("id").equal(id);
		this.deleteByQuery(q);
	}

    public List<SrcDBInstance> findByIp(String ip) {
        Query<SrcDBInstanceMorphia> q = this.getDatastore().createQuery(SrcDBInstanceMorphia.class);
        q.field("entity.metaHost").equal(ip);
        QueryResults<SrcDBInstanceMorphia> result = this.find(q);
        List<SrcDBInstanceMorphia> morphiaEntities = result.asList();

        List<SrcDBInstance> entities = new ArrayList<SrcDBInstance>();
        for (SrcDBInstanceMorphia morphiaEntity : morphiaEntities) {
            entities.add(morphiaEntity.getEntity());
        }
        return entities;
    }
}
