package com.dianping.puma.core.service.impl;

import com.dianping.puma.core.dao.SrcDBInstanceDao;
import com.dianping.puma.core.entity.SrcDBInstance;
import com.dianping.puma.core.service.SrcDBInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("srcDBInstanceService")
public class SrcDBInstanceServiceImpl implements SrcDBInstanceService {

    @Autowired
    SrcDBInstanceDao srcDbInstanceDao;

    @Override
    public SrcDBInstance find(String id) {
        return srcDbInstanceDao.find(id);
    }

    @Override
    public List<SrcDBInstance> findByIp(String ip) {
        return srcDbInstanceDao.findByIp(ip);
    }

    @Override
    public SrcDBInstance findByName(String name) {
        return srcDbInstanceDao.findByName(name);
    }

    @Override
    public List<SrcDBInstance> findAll() {
        return srcDbInstanceDao.findAll();
    }

    @Override
    public void create(SrcDBInstance entity) {
        srcDbInstanceDao.create(entity);
    }

    @Override
    public void update(SrcDBInstance entity) {
        srcDbInstanceDao.update(entity);
    }

    @Override
    public void remove(String id) {
        srcDbInstanceDao.remove(id);
    }
}
