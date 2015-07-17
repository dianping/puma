//package com.dianping.puma.biz.service.impl;
//
//import com.dianping.puma.biz.service.BaseSyncTaskService;
//import com.dianping.puma.biz.service.DumpTaskService;
//import com.dianping.puma.biz.service.SyncTaskService;
//import com.dianping.puma.core.constant.SyncType;
//import com.dianping.puma.biz.entity.old.BaseSyncTask;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//public class BaseSyncTaskServiceImpl implements BaseSyncTaskService {
//
//	@Autowired
//	SyncTaskService syncTaskService;
//
//	@Autowired
//	DumpTaskService dumpTaskService;
//
//	public BaseSyncTask find(SyncType syncType, String name) {
//		switch (syncType) {
//		case SYNC:
//			return syncTaskService.find(name);
//		default:
//			return syncTaskService.find(name);
//		}
//	}
//}
