package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.dao.*;
import com.dianping.puma.biz.entity.PumaTaskDbEntity;
import com.dianping.puma.biz.entity.PumaTaskEntity;
import com.dianping.puma.biz.entity.PumaTaskServerEntity;
import com.dianping.puma.biz.entity.PumaTaskTargetEntity;
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
        List<PumaTaskServerEntity> pumaTaskServers = pumaTaskServerDao.findByTaskId(entity.getId());
        List<Integer> serverIds = Lists.newArrayList(Iterables.transform(pumaTaskServers, new Function<PumaTaskServerEntity, Integer>() {
            @Override
            public Integer apply(PumaTaskServerEntity input) {
                return input.getServerId();
            }
        }));
        entity.setPumaServers(pumaServerDao.findByIds(serverIds));

        List<PumaTaskDbEntity> pumaTaskDbs = pumaTaskDbDao.findByTaskId(entity.getId());
        List<Integer> srcDbIds = Lists.newArrayList(Iterables.transform(pumaTaskDbs, new Function<PumaTaskDbEntity, Integer>() {
            @Override
            public Integer apply(PumaTaskDbEntity input) {
                return input.getDbId();
            }
        }));
        entity.setSrcDbs(srcDbDao.findByIds(srcDbIds));

        List<PumaTaskTargetEntity> targets = pumaTaskTargetDao.findByTaskId(entity.getId());
        TableSet tableSet = new TableSet();
        for (PumaTaskTargetEntity target : targets) {
            tableSet.add(new Table(target.getDatabase(), target.getTables()));
        }
        entity.setTableSet(tableSet);
        return entity;
    }

    protected List<PumaTaskEntity> loadFullPumaTask(List<PumaTaskEntity> entities) {
        for (PumaTaskEntity entity : entities) {
            loadFullPumaTask(entity);
        }
        return entities;
    }

    protected PumaTaskEntity updateFullPumaTask(PumaTaskEntity entity) {
        return entity;
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
        //todo:
    }

    @Override
    public void update(PumaTaskEntity pumaTask) {
        pumaTaskDao.update(pumaTask);
        //todo:
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
