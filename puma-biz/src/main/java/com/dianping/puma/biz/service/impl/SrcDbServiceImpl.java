package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.dao.SrcDbDao;
import com.dianping.puma.biz.entity.SrcDbEntity;
import com.dianping.puma.biz.service.SrcDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("srcDBService")
public class SrcDbServiceImpl implements SrcDbService {
    @Autowired
    SrcDbDao srcDbDao;

    @Override
    public SrcDbEntity find(String name) {
        return srcDbDao.findByName(name);
    }

    @Override
    public SrcDbEntity find(int id) {
        return srcDbDao.findById(id);
    }

    @Override
    public List<SrcDbEntity> findAll() {
        return srcDbDao.findAll();
    }

    @Override
    public long count() {
        return srcDbDao.count();
    }

    @Override
    public List<SrcDbEntity> findByPage(int page, int pageSize) {
        return srcDbDao.findByPage((page - 1) * pageSize, pageSize);
    }

    @Override
    public void create(SrcDbEntity srcDBInstance) {
        srcDbDao.insert(srcDBInstance);
    }

    @Override
    public void update(SrcDbEntity srcDBInstance) {
        srcDbDao.update(srcDBInstance);
    }

    @Override
    public void remove(String name) {
        srcDbDao.deleteByName(name);
    }

    @Override
    public void remove(int id) {
        srcDbDao.delete(id);
    }
}
