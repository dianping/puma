package com.dianping.puma.admin.service;

import com.dianping.puma.core.replicate.model.config.ReplicationTaskConfig;
import org.bson.types.ObjectId;

import java.util.List;

public interface ReplicationTaskConfigService {

	ObjectId save(ReplicationTaskConfig replicationTaskConfig);

	List<ReplicationTaskConfig> findAll();
}
