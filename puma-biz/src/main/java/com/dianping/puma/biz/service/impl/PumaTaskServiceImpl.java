package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.dao.*;
import com.dianping.puma.biz.entity.*;
import com.dianping.puma.biz.service.PumaTaskService;
import com.dianping.puma.core.constant.ActionController;
import com.dianping.puma.core.model.Table;
import com.dianping.puma.core.model.TableSet;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class PumaTaskServiceImpl implements PumaTaskService {

    @Autowired
    PumaTaskDao pumaTaskDao;

    @Autowired
    PumaServerDao pumaServerDao;

    @Autowired
    PumaTaskServerDao pumaTaskServerDao;

    @Autowired
    PumaTaskDbDao pumaTaskDbDao;

    @Autowired
    SrcDbDao srcDbDao;

    @Autowired
    PumaTaskTargetDao pumaTaskTargetDao;

//    protected void savePumaServer(PumaTaskEntity entity) {
//        for (PumaServerEntity serverEntity : entity.getPumaServers()) {
//            PumaServerEntity server = pumaServerDao.findByName(serverEntity.getName());
//            if (server != null) {
//                PumaTaskServerEntity taskServerEntity = new PumaTaskServerEntity();
//                taskServerEntity.setTaskId(entity.getId());
//                taskServerEntity.setServerId(serverEntity.getId());
//                pumaTaskServerDao.insert(taskServerEntity);
//            }
//        }
//    }

    protected void saveSrcDb(PumaTaskEntity entity) {
        for (SrcDbEntity dbEntity : entity.getBackUpSrcDbs()) {
            SrcDbEntity srcDb = srcDbDao.findByName(dbEntity.getName());
            if (srcDb != null) {
                PumaTaskDbEntity pumaTaskDbEntity = new PumaTaskDbEntity();
                pumaTaskDbEntity.setTaskId(entity.getId());
                pumaTaskDbEntity.setDbId(srcDb.getId());
                pumaTaskDbDao.insert(pumaTaskDbEntity);
            }
        }
    }

    protected void saveTableSet(PumaTaskEntity entity) {
        for (Table table : entity.getTableSet().listSchemaTables()) {
            PumaTaskTargetEntity target = new PumaTaskTargetEntity();
            target.setTaskId(entity.getId());
            target.setDatabase(table.getSchemaName());
            target.setTable(table.getTableName());
            pumaTaskTargetDao.insert(target);
        }
    }

    protected void updateFullPumaTask(PumaTaskEntity entity) {
        pumaTaskServerDao.deleteByTaskId(entity.getId());
        //savePumaServer(entity);
        pumaTaskDbDao.deleteByTaskId(entity.getId());
        saveSrcDb(entity);
        pumaTaskTargetDao.deleteByTaskId(entity.getId());
        saveTableSet(entity);
    }

    @Override
    public PumaTaskEntity findByName(String name) {
        return loadFullPumaTask(pumaTaskDao.findByName(name));
    }

    @Override
    public PumaTaskEntity findById(int id) {
        return loadFullPumaTask(pumaTaskDao.findById(id));
    }

    @Override
    public List<PumaTaskEntity> findByPumaServerName(String pumaServerName) {
        return loadFullPumaTasks(pumaTaskDao.findByPumaServerName(pumaServerName));
    }

    @Override
    public List<PumaTaskEntity> findAll() {
        return loadFullPumaTasks(pumaTaskDao.findAll());
    }

    @Override
    public List<PumaTaskEntity> findByPage(int page, int pageSize) {
        return loadFullPumaTasks(pumaTaskDao.findByPage(page, pageSize));
    }

    @Override
    public long count() {
        return pumaTaskDao.count();
    }

    @Override
    public void create(PumaTaskEntity pumaTask) {
        pumaTask.setUpdateTime(new Date());
        pumaTaskDao.insert(pumaTask);
        updateFullPumaTask(pumaTask);
    }

    @Override
    public void update(PumaTaskEntity pumaTask) {
        pumaTask.setUpdateTime(new Date());
        pumaTaskDao.update(pumaTask);
        updateFullPumaTask(pumaTask);
    }

    @Override
    public void remove(String name) {
        PumaTaskEntity entity = pumaTaskDao.findByName(name);
        if (entity != null) {
            remove(entity.getId());
        }
    }

    @Override
    public void remove(int id) {
        pumaTaskDao.delete(id);
        pumaTaskDbDao.deleteByTaskId(id);
        pumaTaskServerDao.deleteByTaskId(id);
        pumaTaskTargetDao.deleteByTaskId(id);
    }

    /**
     * Load full puma task.
     *
     * @param pumaTask basic puma task.
     * @return full puma task.
     */
    protected PumaTaskEntity loadFullPumaTask(PumaTaskEntity pumaTask) {
        pumaTask.setPumaServers(loadPumaServers(pumaTask.getId()));
        pumaTask.setPreferredSrcDb(loadPreferredSrcDb(pumaTask.getJdbcRef()));
        pumaTask.setBackUpSrcDbs(loadBackupSrcDbs(pumaTask.getJdbcRef()));
        pumaTask.setTableSet(loadTableSet(pumaTask.getId()));

        return pumaTask;
    }

    /**
     * Load full puma tasks.
     *
     * @param pumaTasks basic puma tasks.
     * @return full puma tasks.
     */
    protected List<PumaTaskEntity> loadFullPumaTasks(List<PumaTaskEntity> pumaTasks) {
        for (PumaTaskEntity pumaTask: pumaTasks) {
            loadFullPumaTask(pumaTask);
        }
        return pumaTasks;
    }

    /**
     * Load puma servers and their action controllers of a puma task.
     *
     * @param taskId task id of the puma task.
     * @return a list of pair contains the puma servers and their action controller.
     */
    protected List<Pair<PumaServerEntity, ActionController>> loadPumaServers(int taskId) {
        List<Pair<PumaServerEntity, ActionController>> results = new ArrayList<Pair<PumaServerEntity, ActionController>>();

        List<PumaTaskServerEntity> entities = pumaTaskServerDao.findByTaskId(taskId);
        for (PumaTaskServerEntity entity: entities) {
            PumaServerEntity pumaServer = pumaServerDao.findById(entity.getServerId());
            results.add(Pair.of(pumaServer, entity.getActionController()));
        }

        return results;
    }

    /**
     * Load preferred source db machine of a puma task.
     *
     * @param jdbcRef jdbcRef of the source db cluster.
     * @return preferred source db machine.
     */
    protected SrcDbEntity loadPreferredSrcDb(String jdbcRef) {
        List<SrcDbEntity> srcDbs = srcDbDao.findByJdbcRef(jdbcRef);
        for (SrcDbEntity srcDb: srcDbs) {
            if (srcDb.isPreferred()) {
                return srcDb;
            }
        }

        return null;
    }

    /**
     * Load backup source db machines of a puma task.
     *
     * @param jdbcRef jdbcRef of the source db cluster.
     * @return backup source db machines.
     */
    protected List<SrcDbEntity> loadBackupSrcDbs(String jdbcRef) {
        List<SrcDbEntity> backupDbs = new ArrayList<SrcDbEntity>();

        List<SrcDbEntity> srcDbs = srcDbDao.findByJdbcRef(jdbcRef);
        for (SrcDbEntity srcDb: srcDbs) {
            if (!srcDb.isPreferred()) {
                backupDbs.add(srcDb);
            }
        }

        return backupDbs;
    }

    /**
     * Load table set of a puma task.
     *
     * @param taskId task id of the puma task.
     * @return table set of the puma task.
     */
    protected TableSet loadTableSet(int taskId) {
        List<PumaTaskTargetEntity> targets = pumaTaskTargetDao.findByTaskId(taskId);
        TableSet tableSet = new TableSet();
        for (PumaTaskTargetEntity target : targets) {
            tableSet.add(new Table(target.getDatabase(), target.getTable()));
        }
        return tableSet;
    }
}
