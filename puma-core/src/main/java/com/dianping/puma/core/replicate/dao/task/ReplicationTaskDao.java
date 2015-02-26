package com.dianping.puma.core.replicate.dao.task;

import com.dianping.puma.core.replicate.model.task.ReplicationTask;
import com.dianping.puma.core.dao.morphia.MongoClient;
import com.google.code.morphia.dao.BasicDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("replicationTaskConfigDao")
public class ReplicationTaskDao extends BasicDAO<ReplicationTask, String> {

	@Autowired
	public ReplicationTaskDao(MongoClient mongoClient) {
		super(mongoClient.getDatastore());
	}
}
