package com.dianping.puma.biz.newservice.impl;

import com.dianping.puma.biz.dao.SyncTaskDao;
import com.dianping.puma.biz.dao.SyncTaskDstDbDao;
import com.dianping.puma.biz.dao.SyncTaskServerDao;
import com.dianping.puma.biz.entity.SyncTaskEntity;
import com.dianping.puma.biz.entity.SyncTaskDstDbEntity;
import com.dianping.puma.biz.entity.sync.relation.SyncTaskServerEntity;
import com.dianping.puma.biz.newservice.SyncTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SyncTaskServiceImpl implements SyncTaskService {

	@Autowired
	SyncTaskDao syncTaskDao;

	@Autowired
	SyncTaskServerDao syncTaskServerDao;

	@Autowired
	SyncTaskDstDbDao syncTaskDstDbDao;

	@Override
	public List<SyncTaskEntity> findBySyncServerHost(String host) {
		return null;
	}

	@Override
	public void create(SyncTaskEntity task) {
		// Insert sync task.
		int rows = syncTaskDao.insert(task);
		if (rows == 0) {
			throw new RuntimeException("create failure.");
		}

		int taskId = task.getId();

		// Insert relations: sync server.
		List<Integer> syncServerIds = task.getSyncServerIds();
		for (int syncServerId: syncServerIds) {
			SyncTaskServerEntity entity = new SyncTaskServerEntity();
			entity.setTaskId(taskId);
			entity.setServerId(syncServerId);
			syncTaskServerDao.insert(entity);
		}

		// Relations, destination db instances.
		List<Integer> dstDbIds = task.getDstDbIds();
		for (int dstDbId: dstDbIds) {
			SyncTaskDstDbEntity entity = new SyncTaskDstDbEntity();
			entity.setTaskId(taskId);
			entity.setDstDbId(dstDbId);
			syncTaskDstDbDao.insert(entity);
		}
	}

	@Override
	public void delete(SyncTaskEntity entity) {

	}
}
