package com.dianping.puma.admin.service.impl;
//package com.dianping.puma.admin.service.impl;
//
//import java.util.List;
//
//import org.bson.types.ObjectId;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import com.dianping.puma.admin.service.SyncTaskService;
//import com.dianping.puma.core.sync.SyncTask;
//import com.dianping.puma.core.sync.dao.SyncTaskDao;
//import com.google.code.morphia.Key;
//import com.google.code.morphia.query.Query;
//
//@Service("syncTaskService")
//public class SyncTaskServiceImpl implements SyncTaskService {
//
//    @Autowired
//    SyncTaskDao syncTaskDao;
//
//    @Override
//    public Long saveSyncTask(SyncTask syncTask) {
//        Key<SyncTask> key = syncTaskDao.save(syncTask);
//        return (Long) key.getId();
//    }
//
//    @Override
//    public List<SyncTask> findSyncTasksBySyncConfigId(List<Long> syncConfigIds) {
//        Query<SyncTask> q = syncTaskDao.getDatastore().createQuery(SyncTask.class);
//        q.field("syncConfigId").in(syncConfigIds);
//        return syncTaskDao.find(q).asList();
//    }
//
//    @Override
//    public SyncTask findSyncTaskBySyncConfigId(ObjectId syncConfigId) {
//        Query<SyncTask> q = syncTaskDao.getDatastore().createQuery(SyncTask.class);
//        q.field("syncConfigId").equal(syncConfigId);
//        return syncTaskDao.findOne(q);
//    }
//
//}
