package com.dianping.puma.core.dao.morphia;

import com.dianping.puma.core.constant.ActionController;
import com.dianping.puma.core.dao.SyncTaskDao;
import com.dianping.puma.core.dao.morphia.helper.MongoClient;
import com.dianping.puma.core.entity.SyncTask;
import com.dianping.puma.core.entity.morphia.SyncTaskMorphia;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;
import com.google.code.morphia.query.UpdateOperations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("syncTaskDao")
public class SyncTaskMorphiaDao extends MongoBaseDao<SyncTaskMorphia> implements SyncTaskDao {

	@Autowired
	public SyncTaskMorphiaDao(MongoClient mongoClient) {
		super(mongoClient.getDatastore());
	}

	public SyncTask find(long id) {
		Query<SyncTaskMorphia> q = this.getDatastore().createQuery(SyncTaskMorphia.class);
		q.field("id").equal(id);
		SyncTaskMorphia syncTaskMorphia = this.findOne(q);
		return (syncTaskMorphia == null) ? null : syncTaskMorphia.getEntity();
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

	public List<SyncTask> findByDstDBInstanceName(String dstDBInstanceName) {
		Query<SyncTaskMorphia> q = this.getDatastore().createQuery(SyncTaskMorphia.class).disableValidation();
		q.field("entity.dstDBInstanceName").equal(dstDBInstanceName);
		QueryResults<SyncTaskMorphia> result = this.find(q);
		List<SyncTaskMorphia> syncTaskMorphias = result.asList();

		List<SyncTask> entities = new ArrayList<SyncTask>();
		for (SyncTaskMorphia syncTaskMorphia: syncTaskMorphias) {
			entities.add(syncTaskMorphia.getEntity());
		}
		return entities;
	}

	public List<SyncTask> findByPumaServerName(String pumaServerName) {
		Query<SyncTaskMorphia> q = this.getDatastore().createQuery(SyncTaskMorphia.class).disableValidation();
		q.field("entity.pumaServerName").equal(pumaServerName);
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

	@Override
	public void create(SyncTask syncTask) {
		SyncTaskMorphia syncTaskMorphia = new SyncTaskMorphia(syncTask);
		this.save(syncTaskMorphia);
		this.getDatastore().ensureIndexes();
	}

	@Override
	public void remove(String name) {
		Query<SyncTaskMorphia> q = this.getDatastore().createQuery(SyncTaskMorphia.class);
		q.field("name").equal(name);
		this.deleteByQuery(q);
	}
	
	@Override
	public void remove(long id) {
		Query<SyncTaskMorphia> q = this.getDatastore().createQuery(SyncTaskMorphia.class);
		q.field("id").equal(id);
		this.deleteByQuery(q);
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

	public long count() {
		Query<SyncTaskMorphia> q = this.getDatastore().createQuery(SyncTaskMorphia.class);
		return this.count(q);
	}

	public List<SyncTask> findByPage(int page, int pageSize) {
		Query<SyncTaskMorphia> q = this.getDatastore().createQuery(SyncTaskMorphia.class);
		q.offset((page - 1) * pageSize);
		q.limit(pageSize);
		QueryResults<SyncTaskMorphia> result = this.find(q);
		List<SyncTaskMorphia> syncTaskMorphias = result.asList();

		List<SyncTask> syncTasks = new ArrayList<SyncTask>();
		for (SyncTaskMorphia syncTaskMorphia : syncTaskMorphias) {
			syncTasks.add(syncTaskMorphia.getEntity());
		}
		return syncTasks;
	}
	
	public void updateStatusAction(String name,ActionController controller){
		SyncTask syncTask = this.find(name);
		syncTask.setController(controller);
		SyncTaskMorphia syncTaskMorphia = new SyncTaskMorphia(syncTask);
		Query<SyncTaskMorphia> q=this.getDatastore().createQuery(SyncTaskMorphia.class);
		q.field("name").equal(syncTaskMorphia.getName());
		UpdateOperations<SyncTaskMorphia> uop=this.getDatastore().createUpdateOperations(SyncTaskMorphia.class);
		uop.set("entity", syncTask);
		this.update(q,uop);
		this.getDatastore().ensureIndexes();
	}
}
