package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.dao.*;
import com.dianping.puma.biz.entity.PumaTaskEntity;
import com.dianping.puma.biz.service.PumaTaskService;
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
    PumaTaskTargetDao pumaTaskTargetDao;

    protected PumaTaskEntity loadFullPumaTask(PumaTaskEntity entity) {
        return entity;
    }

    protected List<PumaTaskEntity> loadFullPumaTask(List<PumaTaskEntity> entities) {
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
        return pumaTaskDao.findBySrcDbName(srcDBInstanceName);
    }

    @Override
    public List<PumaTaskEntity> findByPumaServerName(String pumaServerName) {
        return pumaTaskDao.findByPumaServerName(pumaServerName);
    }

    @Override
    public List<PumaTaskEntity> findAll() {
        return pumaTaskDao.findAll();
    }

    @Override
    public long count() {
        return pumaTaskDao.count();
    }

    @Override
    public List<PumaTaskEntity> findByPage(int page, int pageSize) {
        return pumaTaskDao.findByPage(page, pageSize);
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
