package com.dianping.puma.core.service.impl;

import com.dianping.puma.core.constant.SyncType;
import com.dianping.puma.core.entity.BaseSyncTask;
import com.dianping.puma.core.service.BaseSyncTaskService;
import com.dianping.puma.core.service.DumpTaskService;
import com.dianping.puma.core.service.SyncTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("baseSyncTaskService")
public class BaseSyncTaskServiceImpl implements BaseSyncTaskService {

	@Autowired
	SyncTaskService syncTaskService;

	@Autowired
	DumpTaskService dumpTaskService;

	public BaseSyncTask find(SyncType syncType, String name) {
		switch (syncType) {
		case SYNC:
			return syncTaskService.find(name);
		case DUMP:
			return dumpTaskService.find(name);
		default:
			return syncTaskService.find(name);
		}
	}
}
