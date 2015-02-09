package com.dianping.puma.service;

import java.util.List;

import org.bson.types.ObjectId;

import com.dianping.puma.core.replicate.model.task.ReplicationTask;

public interface ReplicationTaskService {


	ObjectId save(ReplicationTask replicationTask);
	
	void modify(ReplicationTask replicationTask);

	void remove(ObjectId id);
	
	ReplicationTask find(ObjectId id);
	
	List<ReplicationTask> find(String serverName);

	ReplicationTask findByTaskId(String taskId);
	
	List<ReplicationTask> findAll();
	
}
