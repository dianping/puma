package com.dianping.puma.core.replicate.dao.config;

import com.dianping.puma.core.replicate.model.task.ReplicationTask;
import com.dianping.puma.core.sync.dao.MongoClient;
import com.google.code.morphia.dao.BasicDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("replicationTaskConfigDao")
public class ReplicationTaskConfigDao extends BasicDAO<ReplicationTask, String> {

	@Autowired
	public ReplicationTaskConfigDao(MongoClient mongoClient) {
		super(mongoClient.getDatastore());
	}
}
