package com.dianping.puma.biz.service;

import com.dianping.puma.core.constant.SyncType;
import com.dianping.puma.biz.entity.old.BaseSyncTask;

public interface BaseSyncTaskService {

	BaseSyncTask find(SyncType syncType, String name);
}
