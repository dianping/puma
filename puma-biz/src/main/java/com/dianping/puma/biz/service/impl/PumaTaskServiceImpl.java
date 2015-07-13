package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.dao.*;
import com.dianping.puma.biz.entity.*;
import com.dianping.puma.biz.service.PumaTaskService;
import com.dianping.puma.core.model.Table;
import com.dianping.puma.core.model.TableSet;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("pumaTaskService")
public class PumaTaskServiceImpl implements PumaTaskService {

    @Autowired
    com.dianping.puma.biz.olddao.PumaTaskDao oldPumaTaskDao;

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

    protected PumaTaskEntity loadFullPumaTask(PumaTaskEntity entity) {
        List<PumaServerEntity> pumaServers = loadPumaServer(entity.getId());
        entity.setPumaServers(pumaServers);

        List<SrcDbEntity> srcDbs = loadSrcDb(entity.getId());
        entity.setSrcDbs(srcDbs);

        TableSet tableSet = loadTableSet(entity.getId());
        entity.setTableSet(tableSet);
        return entity;
    }

    protected TableSet loadTableSet(int id) {
        List<PumaTaskTargetEntity> targets = pumaTaskTargetDao.findByTaskId(id);
        TableSet tableSet = new TableSet();
        for (PumaTaskTargetEntity target : targets) {
            tableSet.add(new Table(target.getDatabase(), target.getTables()));
        }
        return tableSet;
    }

    protected List<SrcDbEntity> loadSrcDb(int id) {
        List<PumaTaskDbEntity> pumaTaskDbs = pumaTaskDbDao.findByTaskId(id);
        List<Integer> srcDbIds = Lists.newArrayList(Iterables.transform(pumaTaskDbs, new Function<PumaTaskDbEntity, Integer>() {
            @Override
            public Integer apply(PumaTaskDbEntity input) {
                return input.getDbId();
            }
        }));
        return srcDbDao.findByIds(srcDbIds);
    }

    protected List<PumaServerEntity> loadPumaServer(int id) {
        List<PumaTaskServerEntity> pumaTaskServers = pumaTaskServerDao.findByTaskId(id);
        List<Integer> serverIds = Lists.newArrayList(Iterables.transform(pumaTaskServers, new Function<PumaTaskServerEntity, Integer>() {
            @Override
            public Integer apply(PumaTaskServerEntity input) {
                return input.getServerId();
            }
        }));
        return pumaServerDao.findByIds(serverIds);
    }

    protected List<PumaTaskEntity> loadFullPumaTask(List<PumaTaskEntity> entities) {
        for (PumaTaskEntity entity : entities) {
            loadFullPumaTask(entity);
        }
        return entities;
    }

    protected void savePumaServer(PumaTaskEntity entity) {
        for (PumaServerEntity serverEntity : entity.getPumaServers()) {
            PumaServerEntity server = pumaServerDao.findByName(serverEntity.getName());
            if (server != null) {
                PumaTaskServerEntity taskServerEntity = new PumaTaskServerEntity();
                taskServerEntity.setTaskId(entity.getId());
                taskServerEntity.setServerId(serverEntity.getId());
                pumaTaskServerDao.insert(taskServerEntity);
            }
        }
    }

    protected void saveSrcDb(PumaTaskEntity entity) {
        for (SrcDbEntity dbEntity : entity.getSrcdbs()) {
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
            target.setTables(table.getTableName());
            pumaTaskTargetDao.insert(target);
        }
    }

    protected void updateFullPumaTask(PumaTaskEntity entity) {
        pumaTaskServerDao.deleteByTaskId(entity.getId());
        savePumaServer(entity);
        pumaTaskDbDao.deleteByTaskId(entity.getId());
        saveSrcDb(entity);
        pumaTaskTargetDao.deleteByTaskId(entity.getId());
        saveTableSet(entity);
    }

    @Override
    public PumaTaskEntity find(String name) {
        return loadFullPumaTask(pumaTaskDao.findByName(name));
    }

    @Override
    public PumaTaskEntity find(int id) {
        return loadFullPumaTask(pumaTaskDao.findById(id));
    }

    @Override
    public List<PumaTaskEntity> findBySrcDBInstanceName(String srcDBInstanceName) {
        return loadFullPumaTask(pumaTaskDao.findBySrcDbName(srcDBInstanceName));
    }

    @Override
    public List<PumaTaskEntity> findByPumaServerName(String pumaServerName) {
        return loadFullPumaTask(pumaTaskDao.findByPumaServerName(pumaServerName));
    }

    @Override
    public List<PumaTaskEntity> findAll() {
        return loadFullPumaTask(pumaTaskDao.findAll());
    }

    @Override
    public List<PumaTaskEntity> findByPage(int page, int pageSize) {
        return loadFullPumaTask(pumaTaskDao.findByPage(page, pageSize));
    }

    @Override
    public long count() {
        return pumaTaskDao.count();
    }

    @Override
    public void create(PumaTaskEntity pumaTask) {
        pumaTaskDao.insert(pumaTask);
        updateFullPumaTask(pumaTask);
    }

    @Override
    public void update(PumaTaskEntity pumaTask) {
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
}
