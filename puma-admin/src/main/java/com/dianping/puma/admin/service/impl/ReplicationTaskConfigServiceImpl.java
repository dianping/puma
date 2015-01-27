package com.dianping.puma.admin.service.impl;

import com.dianping.puma.admin.service.ReplicationTaskConfigService;
import com.dianping.puma.core.replicate.dao.config.ReplicationTaskConfigDao;
import com.dianping.puma.core.replicate.model.config.ReplicationTaskConfig;
import com.google.code.morphia.Key;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("ReplicationTaskConfigService")
public class ReplicationTaskConfigServiceImpl implements ReplicationTaskConfigService {

	@Autowired
	ReplicationTaskConfigDao replicationTaskConfigDao;

	@Override
	public ObjectId save(ReplicationTaskConfig replicationTaskConfig) {
		Key<ReplicationTaskConfig> key = this.replicationTaskConfigDao.save(replicationTaskConfig);
		this.replicationTaskConfigDao.getDatastore().ensureIndexes();
		return (ObjectId) key.getId();
	}

	@Override
	public List<ReplicationTaskConfig> findAll() {
		Query<ReplicationTaskConfig> q = replicationTaskConfigDao.getDatastore().createQuery(ReplicationTaskConfig.class);
		QueryResults<ReplicationTaskConfig> result = replicationTaskConfigDao.find(q);
		return result.asList();
	}
}
