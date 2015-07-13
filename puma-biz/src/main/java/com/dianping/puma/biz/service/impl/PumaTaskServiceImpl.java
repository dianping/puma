package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.dao.PumaTaskDao;
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

    protected PumaTaskEntity loadFullPumaTask(PumaTaskEntity entity) {
        return null;
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
        return null;
//        return oldPumaTaskDao.findBySrcDBInstanceName(srcDBInstanceName);
    }

    @Override
    public List<PumaTaskEntity> findByPumaServerName(String pumaServerName) {
        return null;
//        return oldPumaTaskDao.findByPumaServerName(pumaServerName);
    }

    @Override
    public List<PumaTaskEntity> findAll() {
        return null;
//        return oldPumaTaskDao.findAll();
    }

    @Override
    public long count() {
        return oldPumaTaskDao.count();
    }

    @Override
    public List<PumaTaskEntity> findByPage(int page, int pageSize) {
        return null;
//        return oldPumaTaskDao.findByPage(page, pageSize);
    }

    @Override
    public void create(PumaTaskEntity pumaTask) {
//        oldPumaTaskDao.create(pumaTask);
    }

    @Override
    public void update(PumaTaskEntity pumaTask) {
//        oldPumaTaskDao.update(pumaTask);
    }

    @Override
    public void remove(String name) {
//        oldPumaTaskDao.remove(name);
    }

    @Override
    public void remove(int id) {
//        oldPumaTaskDao.remove(id);
    }
}
