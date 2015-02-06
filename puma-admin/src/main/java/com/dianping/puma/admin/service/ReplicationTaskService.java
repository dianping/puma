package com.dianping.puma.admin.service;

import com.dianping.puma.core.replicate.model.task.ReplicationTask;
import org.bson.types.ObjectId;

import java.util.List;

public interface ReplicationTaskService {

	ObjectId save(ReplicationTask replicationTask);

	List<ReplicationTask> findAll();

	void remove(ObjectId id);
}
