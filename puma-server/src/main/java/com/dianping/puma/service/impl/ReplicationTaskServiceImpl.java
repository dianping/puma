package com.dianping.puma.service.impl;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.replicate.dao.task.ReplicationTaskDao;
import com.dianping.puma.core.replicate.model.task.ReplicationTask;
import com.dianping.puma.service.ReplicationTaskService;
import com.google.code.morphia.Key;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;

@Service("replicationTaskService")
public class ReplicationTaskServiceImpl implements ReplicationTaskService {

	@Autowired
	ReplicationTaskDao replicationTaskConfigDao;

	@Override
	public ReplicationTask find(ObjectId id) {
		return this.replicationTaskConfigDao.getDatastore().getByKey(
				ReplicationTask.class,
				new Key<ReplicationTask>(ReplicationTask.class, id));
	}

	@Override
	public List<ReplicationTask> find(String serverName) {
		Query<ReplicationTask> q = this.replicationTaskConfigDao.getDatastore()
				.createQuery(ReplicationTask.class);
		q.field("replicationServerName").equal(serverName);
		QueryResults<ReplicationTask> result = this.replicationTaskConfigDao
				.find(q);
		return result.asList();
	}

	@Override
	public ReplicationTask findByTaskId(String taskId) {
		Query<ReplicationTask> q = this.replicationTaskConfigDao.getDatastore()
				.createQuery(ReplicationTask.class);
		q.field("taskId").equal(taskId);
		return this.replicationTaskConfigDao.findOne(q);
	}

	@Override
	public List<ReplicationTask> findAll() {
		Query<ReplicationTask> q = this.replicationTaskConfigDao.getDatastore()
				.createQuery(ReplicationTask.class);
		QueryResults<ReplicationTask> result = this.replicationTaskConfigDao
				.find(q);
		return result.asList();
	}

	@Override
	public void modify(ReplicationTask replicationTask) {
		this.replicationTaskConfigDao.save(replicationTask);
	}

	@Override
	public void remove(ObjectId id) {
		Query<ReplicationTask> q = this.replicationTaskConfigDao.getDatastore()
				.createQuery(ReplicationTask.class);
		q.field("_id").equal(id);
		this.replicationTaskConfigDao.deleteByQuery(q);
	}

	@Override
	public ObjectId save(ReplicationTask replicationTask) {
		Key<ReplicationTask> key = this.replicationTaskConfigDao
				.save(replicationTask);
		return (ObjectId) key.getId();
	}

}
