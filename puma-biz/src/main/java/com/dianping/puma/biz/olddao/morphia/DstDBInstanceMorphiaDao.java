package com.dianping.puma.biz.olddao.morphia;

import com.dianping.puma.biz.olddao.morphia.helper.MongoClient;
import com.dianping.puma.biz.olddao.DstDBInstanceDao;
import com.dianping.puma.biz.entity.old.DstDBInstance;
import com.dianping.puma.biz.entity.morphia.DstDBInstanceMorphia;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;
import com.google.code.morphia.query.UpdateOperations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

public class DstDBInstanceMorphiaDao extends MongoBaseDao<DstDBInstanceMorphia>
		implements DstDBInstanceDao {

	@Autowired
	public DstDBInstanceMorphiaDao(MongoClient mongoClient) {
		super(mongoClient.getDatastore());
	}

	public DstDBInstance find(long id) {
		Query<DstDBInstanceMorphia> q = this.getDatastore().createQuery(DstDBInstanceMorphia.class);
		q.field("id").equal(id);
		DstDBInstanceMorphia dstDBInstanceMorphia = this.findOne(q);
		return (dstDBInstanceMorphia == null) ? null : dstDBInstanceMorphia.getEntity();
	}
	
	public DstDBInstance find(String name) {
		Query<DstDBInstanceMorphia> q = this.getDatastore().createQuery(DstDBInstanceMorphia.class);
		q.field("name").equal(name);
		DstDBInstanceMorphia dstDBInstanceMorphia = this.findOne(q);
		return (dstDBInstanceMorphia == null) ? null : dstDBInstanceMorphia.getEntity();
	}

	public List<DstDBInstance> findAll() {
		Query<DstDBInstanceMorphia> q = this.getDatastore().createQuery(DstDBInstanceMorphia.class);
		QueryResults<DstDBInstanceMorphia> result = this.find(q);
		List<DstDBInstanceMorphia> dstDBInstanceMorphias = result.asList();

		List<DstDBInstance> dstDBInstances = new ArrayList<DstDBInstance>();
		for(DstDBInstanceMorphia dstDBInstanceMorphia: dstDBInstanceMorphias) {
			dstDBInstances.add(dstDBInstanceMorphia.getEntity());
		}
		return dstDBInstances;
	}

	public long count() {
		Query<DstDBInstanceMorphia> q = this.getDatastore().createQuery(DstDBInstanceMorphia.class);
		return this.count(q);
	}

	public List<DstDBInstance> findByPage(int page, int pageSize) {
		Query<DstDBInstanceMorphia> q = this.getDatastore().createQuery(DstDBInstanceMorphia.class);
		q.offset((page - 1) * pageSize);
		q.limit(pageSize);
		QueryResults<DstDBInstanceMorphia> result = this.find(q);
		List<DstDBInstanceMorphia> dstDBInstanceMorphias = result.asList();

		List<DstDBInstance> dstDBInstances = new ArrayList<DstDBInstance>();
		for (DstDBInstanceMorphia dstDBInstanceMorphia : dstDBInstanceMorphias) {
			dstDBInstances.add(dstDBInstanceMorphia.getEntity());
		}
		return dstDBInstances;
	}
	
	public void create(DstDBInstance dstDBInstance) {
		DstDBInstanceMorphia dstDBInstanceMorphia = new DstDBInstanceMorphia(dstDBInstance);
		this.save(dstDBInstanceMorphia);
		this.getDatastore().ensureIndexes();
	}

	public void update(DstDBInstance dstDBInstance) {
		DstDBInstanceMorphia dstDBInstanceMorphia = new DstDBInstanceMorphia(dstDBInstance);
		Query<DstDBInstanceMorphia> q = this.getDatastore().createQuery(DstDBInstanceMorphia.class);
		q.field("name").equal(dstDBInstanceMorphia.getName());
		UpdateOperations<DstDBInstanceMorphia> uop = this.getDatastore().createUpdateOperations(DstDBInstanceMorphia.class);
		uop.set("entity", dstDBInstance);
		this.update(q, uop);
		this.getDatastore().ensureIndexes();
	}

	public void remove(String name) {
		Query<DstDBInstanceMorphia> q = this.getDatastore().createQuery(DstDBInstanceMorphia.class);
		q.field("name").equal(name);
		this.deleteByQuery(q);
	}

	public void remove(long id) {
		Query<DstDBInstanceMorphia> q = this.getDatastore().createQuery(DstDBInstanceMorphia.class);
		q.field("id").equal(id);
		this.deleteByQuery(q);
	}
}
