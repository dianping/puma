package com.dianping.puma.biz.dao.morphia;

import com.dianping.puma.biz.dao.DumpTaskDao;
import com.dianping.puma.biz.dao.morphia.helper.MongoClient;
import com.dianping.puma.biz.entity.DumpTask;
import com.dianping.puma.biz.entity.morphia.DumpTaskMorphia;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("dumpTaskDao")
public class DumpTaskMorphiaDao extends MongoBaseDao<DumpTaskMorphia> implements DumpTaskDao {

	@Autowired
	public DumpTaskMorphiaDao(MongoClient mongoClient) {
		super(mongoClient.getDatastore());
	}
	
	public DumpTask find(long id) {
		Query<DumpTaskMorphia> q = this.getDatastore().createQuery(DumpTaskMorphia.class);
		q.field("id").equal(id);
		DumpTaskMorphia syncTaskMorphia = this.findOne(q);
		return (syncTaskMorphia == null) ? null : syncTaskMorphia.getEntity();
	}

	public DumpTask find(String name) {
		Query<DumpTaskMorphia> q = this.getDatastore().createQuery(DumpTaskMorphia.class);
		q.field("name").equal(name);
		DumpTaskMorphia syncTaskMorphia = this.findOne(q);
		return (syncTaskMorphia == null) ? null : syncTaskMorphia.getEntity();
	}

	public List<DumpTask> findAll() {
		Query<DumpTaskMorphia> q = this.getDatastore().createQuery(DumpTaskMorphia.class);
		QueryResults<DumpTaskMorphia> result = this.find(q);
		List<DumpTaskMorphia> syncTaskMorphias = result.asList();

		List<DumpTask> syncTasks = new ArrayList<DumpTask>();
		for(DumpTaskMorphia syncTaskMorphia: syncTaskMorphias) {
			syncTasks.add(syncTaskMorphia.getEntity());
		}
		return syncTasks;
	}

	public void create(DumpTask syncTask) {
		DumpTaskMorphia syncTaskMorphia = new DumpTaskMorphia(syncTask);
		this.save(syncTaskMorphia);
		this.getDatastore().ensureIndexes();
	}

	public void remove(String name) {
		Query<DumpTaskMorphia> q = this.getDatastore().createQuery(DumpTaskMorphia.class);
		q.field("name").equal(name);
		this.deleteByQuery(q);
	}
	
	public void remove(long id) {
		Query<DumpTaskMorphia> q = this.getDatastore().createQuery(DumpTaskMorphia.class);
		q.field("id").equal(id);
		this.deleteByQuery(q);
	}
}
