package com.dianping.puma.service.impl;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.puma.core.server.dao.ServerTaskDao;
import com.dianping.puma.core.server.model.ServerTask;
import com.dianping.puma.service.ServerTaskService;
import com.google.code.morphia.Key;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;

@Service("serverTaskService")
public class ServerTaskServiceImpl implements
		ServerTaskService {

	@Autowired
	ServerTaskDao serverTaskDao;

	public void setServerTaskDao(
			ServerTaskDao serverTaskDao) {
		this.serverTaskDao = serverTaskDao;
	}

	@Override
	public ObjectId save(ServerTask serverTask) {
		Key<ServerTask> key = this.serverTaskDao
				.save(serverTask);
		return (ObjectId) key.getId();
	}

	@Override
	public void modify(ServerTask serverTask) {
		this.serverTaskDao.save(serverTask);
	}

	@Override
	public void remove(ObjectId id) {
		Query<ServerTask> q = this.serverTaskDao
				.getDatastore().createQuery(ServerTask.class);
		q.field("_id").equal(id);
		this.serverTaskDao.deleteByQuery(q);
	}

	@Override
	public ServerTask find(ObjectId id) {
		return this.serverTaskDao.getDatastore().getByKey(
				ServerTask.class,
				new Key<ServerTask>(ServerTask.class, id));
	}

	@Override
	public List<ServerTask> find(String serverName) {
		Query<ServerTask> q = this.serverTaskDao
				.getDatastore().createQuery(ServerTask.class);
		q.field("serverName").equal(serverName);
		QueryResults<ServerTask> result = this.serverTaskDao
				.find(q);
		return result.asList();
	}

	@Override
	public ServerTask find(long taskId) {
		Query<ServerTask> q = this.serverTaskDao
				.getDatastore().createQuery(ServerTask.class);
		q.field("taskId").equal(taskId);
		return this.serverTaskDao.findOne(q);
		 
	}

	
	@Override
	public List<ServerTask> findAll() {
		Query<ServerTask> q = this.serverTaskDao
				.getDatastore().createQuery(ServerTask.class);
		QueryResults<ServerTask> result = this.serverTaskDao
				.find(q);
		return result.asList();
	}
}
