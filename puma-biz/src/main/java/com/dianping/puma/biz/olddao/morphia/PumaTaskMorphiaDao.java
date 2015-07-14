package com.dianping.puma.biz.olddao.morphia;

import com.dianping.puma.biz.olddao.PumaTaskDao;
import com.dianping.puma.biz.olddao.morphia.helper.MongoClient;
import com.dianping.puma.biz.entity.old.PumaTask;
import com.dianping.puma.biz.entity.morphia.PumaTaskMorphia;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;
import com.google.code.morphia.query.UpdateOperations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

public class PumaTaskMorphiaDao extends MongoBaseDao<PumaTaskMorphia> implements PumaTaskDao {

	@Autowired
	public PumaTaskMorphiaDao(MongoClient mongoClient) {
		super(mongoClient.getDatastore());
	}

	public PumaTask find(long id) {
		Query<PumaTaskMorphia> q = this.getDatastore().createQuery(PumaTaskMorphia.class);
		q.field("id").equal(id);
		PumaTaskMorphia pumaTaskMorphia = this.findOne(q);
		return (pumaTaskMorphia == null) ? null : pumaTaskMorphia.getEntity();
	}

	public PumaTask find(String name) {
		Query<PumaTaskMorphia> q = this.getDatastore().createQuery(PumaTaskMorphia.class);
		q.field("name").equal(name);
		PumaTaskMorphia pumaTaskMorphia = this.findOne(q);
		return (pumaTaskMorphia == null) ? null : pumaTaskMorphia.getEntity();
	}

	public List<PumaTask> findBySrcDBInstanceName(String srcDBInstanceName) {
		Query<PumaTaskMorphia> q = this.getDatastore().createQuery(PumaTaskMorphia.class).disableValidation();
		q.field("entity.srcDBInstanceName").equal(srcDBInstanceName);
		QueryResults<PumaTaskMorphia> result = this.find(q);
		List<PumaTaskMorphia> pumaTaskMorphias = result.asList();

		List<PumaTask> pumaTasks = new ArrayList<PumaTask>();
		for (PumaTaskMorphia pumaTaskMorphia: pumaTaskMorphias) {
			pumaTasks.add(pumaTaskMorphia.getEntity());
		}
		return pumaTasks;
	}

	public List<PumaTask> findByPumaServerName(String pumaServerName) {
		Query<PumaTaskMorphia> q = this.getDatastore().createQuery(PumaTaskMorphia.class).disableValidation();
		q.field("entity.pumaServerName").equal(pumaServerName);
		QueryResults<PumaTaskMorphia> result = this.find(q);
		List<PumaTaskMorphia> pumaTaskMorphias = result.asList();

		List<PumaTask> pumaTasks = new ArrayList<PumaTask>();
		for (PumaTaskMorphia pumaTaskMorphia: pumaTaskMorphias) {
			pumaTasks.add(pumaTaskMorphia.getEntity());
		}
		return pumaTasks;
	}
	
	public List<PumaTask> findByPumaServerNames(String pumaServerName) {
		Query<PumaTaskMorphia> q = this.getDatastore().createQuery(PumaTaskMorphia.class).disableValidation();
		q.field("entity.pumaServerNames").contains(pumaServerName);
		QueryResults<PumaTaskMorphia> result = this.find(q);
		List<PumaTaskMorphia> pumaTaskMorphias = result.asList();

		List<PumaTask> pumaTasks = new ArrayList<PumaTask>();
		for (PumaTaskMorphia pumaTaskMorphia: pumaTaskMorphias) {
			pumaTasks.add(pumaTaskMorphia.getEntity());
		}
		return pumaTasks;
	}

	public List<PumaTask> findAll() {
		Query<PumaTaskMorphia> q = this.getDatastore().createQuery(PumaTaskMorphia.class);
		QueryResults<PumaTaskMorphia> result = this.find(q);
		List<PumaTaskMorphia> pumaTaskMorphias = result.asList();

		List<PumaTask> pumaTasks = new ArrayList<PumaTask>();
		for (PumaTaskMorphia pumaTaskMorphia: pumaTaskMorphias) {
			pumaTasks.add(pumaTaskMorphia.getEntity());
		}
		return pumaTasks;
	}

	public long count() {
		Query<PumaTaskMorphia> q = this.getDatastore().createQuery(PumaTaskMorphia.class);
		return this.count(q);
	}

	public List<PumaTask> findByPage(int page, int pageSize) {
		Query<PumaTaskMorphia> q = this.getDatastore().createQuery(PumaTaskMorphia.class);
		q.offset((page - 1) * pageSize);
		q.limit(pageSize);
		QueryResults<PumaTaskMorphia> result = this.find(q);
		List<PumaTaskMorphia> pumaTaskMorphias = result.asList();

		List<PumaTask> pumaTasks = new ArrayList<PumaTask>();
		for (PumaTaskMorphia pumaTaskMorphia : pumaTaskMorphias) {
			pumaTasks.add(pumaTaskMorphia.getEntity());
		}
		return pumaTasks;
	}
	
	public void create(PumaTask pumaTask) {
		PumaTaskMorphia pumaTaskMorphia = new PumaTaskMorphia(pumaTask);
		this.save(pumaTaskMorphia);
		this.getDatastore().ensureIndexes();
	}

	public void update(PumaTask pumaTask) {
		PumaTaskMorphia pumaTaskMorphia = new PumaTaskMorphia(pumaTask);
		Query<PumaTaskMorphia> q = this.getDatastore().createQuery(PumaTaskMorphia.class);
		q.field("name").equal(pumaTaskMorphia.getName());
		UpdateOperations<PumaTaskMorphia> uop = this.getDatastore().createUpdateOperations(PumaTaskMorphia.class);
		uop.set("entity", pumaTask);
		this.update(q, uop);
		this.getDatastore().ensureIndexes();
	}

	public void remove(String name) {
		Query<PumaTaskMorphia> q = this.getDatastore().createQuery(PumaTaskMorphia.class);
		q.field("name").equal(name);
		this.deleteByQuery(q);
	}
	
	public void remove(long id) {
		Query<PumaTaskMorphia> q = this.getDatastore().createQuery(PumaTaskMorphia.class);
		q.field("id").equal(id);
		this.deleteByQuery(q);
	}
}
