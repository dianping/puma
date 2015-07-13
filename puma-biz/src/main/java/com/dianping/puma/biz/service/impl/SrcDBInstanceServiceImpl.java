package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.entity.SrcDbEntity;
import com.dianping.puma.biz.olddao.SrcDBInstanceDao;
import com.dianping.puma.biz.service.SrcDBInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("srcDBInstanceService")
public class SrcDBInstanceServiceImpl implements SrcDBInstanceService {
    @Autowired
    SrcDBInstanceDao srcDbInstanceDao;

    @Override
    public SrcDbEntity find(String name) {
        return null;
//		return srcDbInstanceDao.find(name);
    }

    @Override
    public SrcDbEntity find(long id) {
        return null;
//        return srcDbInstanceDao.find(id);
    }

    @Override
    public List<SrcDbEntity> findAll() {
        return null;

//        return srcDbInstanceDao.findAll();
    }

    @Override
    public long count() {
        return srcDbInstanceDao.count();
    }

    @Override
    public List<SrcDbEntity> findByPage(int page, int pageSize) {
        return null;

//        return srcDbInstanceDao.findByPage(page, pageSize);
    }

    @Override
    public void create(SrcDbEntity srcDBInstance) {

        //srcDbInstanceDao.create(srcDBInstance);
    }

    @Override
    public void update(SrcDbEntity srcDBInstance) {

        //srcDbInstanceDao.update(srcDBInstance);
    }

    @Override
    public void remove(String name) {
        srcDbInstanceDao.remove(name);
    }

    @Override
    public void remove(int id) {
        srcDbInstanceDao.remove(id);
    }

    @Override
    public List<SrcDbEntity> findByIp(String ip) {
        return null;
        //return srcDbInstanceDao.findByIp(ip);
    }
}
