package com.dianping.puma.core.dao.morphia;

import com.dianping.puma.core.dao.SyncTaskDao;
import com.dianping.puma.core.entity.SyncTask;
import com.dianping.puma.core.entity.morphia.SyncTaskMorphia;
import com.google.code.morphia.dao.BasicDAO;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("syncTaskDao")
public class SyncTaskMorphiaDao extends BasicDAO<SyncTaskMorphia, String> implements SyncTaskDao {

	@Autowired
	public SyncTaskMorphiaDao(MongoClient mongoClient) {
		super(mongoClient.getDatastore());
	}

	public SyncTask find(String name) {
		Query<SyncTaskMorphia> q = this.getDatastore().createQuery(SyncTaskMorphia.class);
		q.field("name").equal(name);
		SyncTaskMorphia syncTaskMorphia = this.findOne(q);
		return (syncTaskMorphia == null) ? null : syncTaskMorphia.getEntity();
	}

	public List<SyncTask> findBySyncServerName(String syncServerName) {
		Query<SyncTaskMorphia> q = this.getDatastore().createQuery(SyncTaskMorphia.class).disableValidation();
		q.field("entity.syncServerName").equal(syncServerName);
		QueryResults<SyncTaskMorphia> result = this.find(q);
		List<SyncTaskMorphia> syncTaskMorphias = result.asList();

		List<SyncTask> entities = new ArrayList<SyncTask>();
		for (SyncTaskMorphia syncTaskMorphia: syncTaskMorphias) {
			entities.add(syncTaskMorphia.getEntity());
		}
		return entities;
	}

	public List<SyncTask> findAll() {
		Query<SyncTaskMorphia> q = this.getDatastore().createQuery(SyncTaskMorphia.class);
		QueryResults<SyncTaskMorphia> result = this.find(q);
		List<SyncTaskMorphia> syncTaskMorphias = result.asList();

		List<SyncTask> syncTasks = new ArrayList<SyncTask>();
		for(SyncTaskMorphia syncTaskMorphia: syncTaskMorphias) {
			syncTasks.add(syncTaskMorphia.getEntity());
		}
		return syncTasks;
	}

	public void create(SyncTask syncTask) {
		SyncTaskMorphia syncTaskMorphia = new SyncTaskMorphia(syncTask);
		this.save(syncTaskMorphia);
		this.getDatastore().ensureIndexes();
	}
	@Override
	public List<SyncTask> find(int offset, int limit) {
		Query<SyncTaskMorphia> q = this.getDatastore().createQuery(SyncTaskMorphia.class);
		q.offset(offset);
		q.limit(limit);
		QueryResults<SyncTaskMorphia> result = this.find(q);
		List<SyncTask> syncTasks = new ArrayList<SyncTask>();
		for(SyncTaskMorphia syncTaskMorphia: result.asList()) {
			syncTasks.add(syncTaskMorphia.getEntity());
		}
		return syncTasks;
	}
}
