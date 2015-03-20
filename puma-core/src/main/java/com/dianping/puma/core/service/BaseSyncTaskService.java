package com.dianping.puma.core.service;

import com.dianping.puma.core.constant.SyncType;
import com.dianping.puma.core.entity.BaseSyncTask;

public interface BaseSyncTaskService {

	BaseSyncTask find(SyncType syncType, String name);
}
