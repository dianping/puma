package com.dianping.puma.admin.service.impl;

import com.dianping.puma.admin.service.ReplicationTaskService;
import com.dianping.puma.core.monitor.ReplicationTaskEvent;
import com.dianping.puma.core.monitor.SwallowEventPublisher;
import com.dianping.puma.core.monitor.TaskEvent;
import com.dianping.puma.core.replicate.dao.config.ReplicationTaskConfigDao;
import com.dianping.puma.core.replicate.model.task.ReplicationTask;
import com.dianping.puma.core.replicate.model.task.Type;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;
import com.google.code.morphia.Key;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryResults;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("ReplicationTaskConfigService")
public class ReplicationTaskServiceImpl implements ReplicationTaskService {

	@Autowired
	ReplicationTaskConfigDao replicationTaskConfigDao;

	@Autowired
	SwallowEventPublisher taskEventPublisher;

	@Override
	public ObjectId save(ReplicationTask replicationTask) {
		Key<ReplicationTask> key = this.replicationTaskConfigDao.save(replicationTask);
		this.replicationTaskConfigDao.getDatastore().ensureIndexes();

		ReplicationTaskEvent event = new ReplicationTaskEvent();
		event.setType(Type.REPLICATION);
		event.setTaskId((Long) key.getId());

		try {
			taskEventPublisher.publish(event);
		}
		catch(SendFailedException e) {
			throw new RuntimeException("已经创建任务，但给Puma Server发送通知失败，Puma Server需要在重启后才能感知。或者您可以删除然后重启创建任务！");
		}

		return (ObjectId) key.getId();
	}

	@Override
	public List<ReplicationTask> findAll() {
		Query<ReplicationTask> q = replicationTaskConfigDao.getDatastore().createQuery(ReplicationTask.class);
		QueryResults<ReplicationTask> result = replicationTaskConfigDao.find(q);
		return result.asList();
	}
}
