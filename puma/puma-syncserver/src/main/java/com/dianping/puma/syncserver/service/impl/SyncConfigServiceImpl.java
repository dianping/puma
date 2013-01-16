//package com.dianping.puma.syncserver.service.impl;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.apache.commons.collections.ListUtils;
//import org.bson.types.ObjectId;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import com.dianping.puma.core.sync.SyncConfig;
//import com.dianping.puma.core.sync.SyncTask;
//import com.dianping.puma.core.sync.dao.SyncConfigDao;
//import com.dianping.puma.core.sync.dao.SyncTaskDao;
//import com.dianping.puma.syncserver.service.SyncConfigService;
//import com.google.code.morphia.Key;
//import com.google.code.morphia.query.Query;
//import com.google.code.morphia.query.QueryResults;
//
//@Service("syncConfigService")
//public class SyncConfigServiceImpl implements SyncConfigService {
//    @Autowired
//    SyncConfigDao syncConfigDao;
//
//    @Autowired
//    SyncTaskDao syncTaskDao;
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public List<SyncConfig> findSyncConfigs(String pumaSyncServerId) {
//        //查询task
//        Query<SyncTask> q = syncTaskDao.getDatastore().createQuery(SyncTask.class);
//        q.field("pumaSyncServerId").equal(pumaSyncServerId);
//        QueryResults<SyncTask> result = syncTaskDao.find(q);
//        List<SyncTask> syncTasks = result.asList();
//        if (syncTasks.size() > 0) {
//            List<ObjectId> syncConfigIds = new ArrayList<ObjectId>();
//            for (SyncTask syncTask : syncTasks) {
//                syncConfigIds.add(syncTask.getSyncConfigId());
//            }
//            //根据task查询SyncConfig
//            Query<SyncConfig> q2 = syncConfigDao.getDatastore().createQuery(SyncConfig.class);
//            q2.field("pumaSyncServerId").in(syncConfigIds);
//            QueryResults<SyncConfig> syncConfigResult = syncConfigDao.find(q2);
//            return syncConfigResult.asList();
//        }
//        return ListUtils.EMPTY_LIST;
//    }
//
//    @Override
//    public SyncConfig findSyncConfig(ObjectId taskId) {
//        //查询task
//        SyncTask task = syncTaskDao.getDatastore().getByKey(SyncTask.class, new Key<SyncTask>(SyncTask.class, taskId));
//        if (task != null) {
//            //根据task查询SyncConfig
//            ObjectId syncConfigId = task.getSyncConfigId();
//            return syncConfigDao.getDatastore().getByKey(SyncConfig.class, new Key<SyncConfig>(SyncConfig.class, syncConfigId));
//        }
//        return null;
//    }
//
//}
