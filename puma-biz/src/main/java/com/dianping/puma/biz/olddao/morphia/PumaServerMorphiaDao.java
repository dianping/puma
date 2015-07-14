package com.dianping.puma.biz.olddao.morphia;

import com.dianping.puma.biz.olddao.PumaServerDao;
import com.dianping.puma.biz.olddao.morphia.helper.MongoClient;
import com.dianping.puma.biz.entity.old.PumaServer;
import com.dianping.puma.biz.entity.morphia.PumaServerMorphia;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;
import com.google.code.morphia.query.UpdateOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

public class PumaServerMorphiaDao extends MongoBaseDao<PumaServerMorphia> implements PumaServerDao {

	@Autowired
	public PumaServerMorphiaDao(MongoClient mongoClient) {
		super(mongoClient.getDatastore());
	}

	public PumaServer find(long id) {
		Query<PumaServerMorphia> q = this.getDatastore().createQuery(PumaServerMorphia.class);
		q.field("id").equal(id);
		PumaServerMorphia pumaServerMorphia = this.findOne(q);
		return (pumaServerMorphia == null) ? null : pumaServerMorphia.getEntity();
	}

	public PumaServer find(String name) {
		Query<PumaServerMorphia> q = this.getDatastore().createQuery(PumaServerMorphia.class);
		q.field("name").equal(name);
		PumaServerMorphia pumaServerMorphia = this.findOne(q);
		return (pumaServerMorphia == null) ? null : pumaServerMorphia.getEntity();
	}

	public PumaServer findByHost(String host) {
		Query<PumaServerMorphia> q = this.getDatastore().createQuery(PumaServerMorphia.class).disableValidation();
		q.field("entity.host").equal(host);
		PumaServerMorphia pumaServerMorphia = this.findOne(q);
		return (pumaServerMorphia == null) ? null : pumaServerMorphia.getEntity();
	}

	public List<PumaServer> findAll() {
		Query<PumaServerMorphia> q = this.getDatastore().createQuery(PumaServerMorphia.class);
		QueryResults<PumaServerMorphia> result = this.find(q);
		List<PumaServerMorphia> pumaServerMorphias = result.asList();

		List<PumaServer> pumaServerEntities = new ArrayList<PumaServer>();
		for (PumaServerMorphia pumaServerMorphia : pumaServerMorphias) {
			pumaServerEntities.add(pumaServerMorphia.getEntity());
		}
		return pumaServerEntities;
	}

	public long count() {
		Query<PumaServerMorphia> q = this.getDatastore().createQuery(PumaServerMorphia.class);
		return this.count(q);
	}

	public List<PumaServer> findByPage(int page, int pageSize) {
		Query<PumaServerMorphia> q = this.getDatastore().createQuery(PumaServerMorphia.class);
		q.offset((page - 1) * pageSize);
		q.limit(pageSize);
		QueryResults<PumaServerMorphia> result = this.find(q);
		List<PumaServerMorphia> pumaServerMorphias = result.asList();

		List<PumaServer> pumaServerEntities = new ArrayList<PumaServer>();
		for (PumaServerMorphia pumaServerMorphia : pumaServerMorphias) {
			pumaServerEntities.add(pumaServerMorphia.getEntity());
		}
		return pumaServerEntities;
	}

	public void create(PumaServer pumaServer) {
		PumaServerMorphia pumaServerMorphia = new PumaServerMorphia(pumaServer);
		this.save(pumaServerMorphia);
		this.getDatastore().ensureIndexes();
	}

	public void update(PumaServer pumaServer) {
		PumaServerMorphia pumaServerMorphia = new PumaServerMorphia(pumaServer);
		Query<PumaServerMorphia> q = this.getDatastore().createQuery(PumaServerMorphia.class);
		q.field("name").equal(pumaServerMorphia.getName());
		UpdateOperations<PumaServerMorphia> uop = this.getDatastore().createUpdateOperations(PumaServerMorphia.class);
		uop.set("entity", pumaServer);
		this.update(q, uop);
		this.getDatastore().ensureIndexes();
	}

	public void remove(String name) {
		Query<PumaServerMorphia> q = this.getDatastore().createQuery(PumaServerMorphia.class);
		q.field("name").equal(name);
		this.deleteByQuery(q);
	}

	public void remove(long id) {
		Query<PumaServerMorphia> q = this.getDatastore().createQuery(PumaServerMorphia.class);
		q.field("id").equal(id);
		this.deleteByQuery(q);
	}
}
