package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.dao.PumaServerDao;
import com.dianping.puma.biz.entity.PumaServerEntity;
import com.dianping.puma.biz.service.PumaServerService;
import com.dianping.puma.utils.IPUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service("pumaServerService")
public class PumaServerServiceImpl implements PumaServerService {

    @Autowired
    PumaServerDao pumaServerDao;

    @Override
    public PumaServerEntity find(String name) {
        return pumaServerDao.findByName(name);
    }

    @Override
    public PumaServerEntity findById(int id) {
        return pumaServerDao.findById(id);
    }

    @Override
    public PumaServerEntity findByHost(String host) {
        return pumaServerDao.findByHost(host);
    }

    @Override
    public List<PumaServerEntity> findOnCurrentServer() {
        List<PumaServerEntity> result = new ArrayList<PumaServerEntity>();
        for (String host : IPUtils.getNoLoopbackIP4Addresses()) {
            PumaServerEntity entity = findByHost(host);
            if (entity != null) {
                result.add(entity);
            }
        }
        return result;
    }

    @Override
    public List<PumaServerEntity> findAll() {
        return pumaServerDao.findAll();
    }

    @Override
    public List<PumaServerEntity> findAllAlive() {
        return pumaServerDao.findAllAlive();
    }

    @Override
    public List<PumaServerEntity> findByPage(int page, int pageSize) {
        return pumaServerDao.findByPage((page - 1) * pageSize, pageSize);
    }

    @Override
    public long count() {
        return pumaServerDao.count();
    }

    @Override
    public void registerByHost(String host) {
        PumaServerEntity server = findByHost(host);
        if (server == null) {
            server = new PumaServerEntity();
            server.setName(host);
            server.setHost(host);
            server.setPort(4040);
            create(server);
        } else {
            server.setUpdateTime(new Date());
            update(server);
        }
    }

    @Override
    public void create(PumaServerEntity pumaServer) {
        pumaServerDao.insert(pumaServer);
    }

    @Override
    public void update(PumaServerEntity pumaServer) {
        pumaServerDao.update(pumaServer);
    }

    @Override
    public void remove(String name) {
        pumaServerDao.deleteByName(name);
    }

    @Override
    public void remove(int id) {
        pumaServerDao.delete(id);
    }
}
