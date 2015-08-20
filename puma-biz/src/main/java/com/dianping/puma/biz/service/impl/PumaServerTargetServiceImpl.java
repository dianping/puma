package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.dao.PumaServerTargetDao;
import com.dianping.puma.biz.entity.PumaServerTargetEntity;
import com.dianping.puma.biz.service.PumaServerTargetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PumaServerTargetServiceImpl implements PumaServerTargetService {

    @Autowired
    PumaServerTargetDao pumaServerTargetDao;

    @Override
    public List<PumaServerTargetEntity> findByDatabase(String database) {
        return pumaServerTargetDao.findByDatabase(database);
    }

    @Override
    public List<PumaServerTargetEntity> findByServerName(String host) {
        return pumaServerTargetDao.findByServerName(host);
    }

    @Override
    public int create(PumaServerTargetEntity entity) {
        return pumaServerTargetDao.insert(entity);
    }

    @Override
    public int update(PumaServerTargetEntity entity) {
        return pumaServerTargetDao.update(entity);
    }

    @Override
    public int remove(int id) {
        return pumaServerTargetDao.delete(id);
    }
}
