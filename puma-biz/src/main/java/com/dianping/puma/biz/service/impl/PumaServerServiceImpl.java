package com.dianping.puma.biz.service.impl;

import com.dianping.cat.Cat;
import com.dianping.puma.biz.dao.PumaServerDao;
import com.dianping.puma.biz.entity.PumaServerEntity;
import com.dianping.puma.biz.service.PumaServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
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
    public PumaServerEntity find(int id) {
        return pumaServerDao.findById(id);
    }

    public PumaServerEntity findByHost(String host) {
        return pumaServerDao.findByHost(host);
    }

    @Override
    public List<PumaServerEntity> findAll() {
        return pumaServerDao.findAll();
    }

    @Override
    public long count() {
        return pumaServerDao.count();
    }

    @Override
    public List<PumaServerEntity> findByPage(int page, int pageSize) {
        return pumaServerDao.findByPage((page - 1) * pageSize, pageSize);
    }

    @Override
    public void heartBeat() {
        try {
            PumaServerEntity server = findByHost(InetAddress.getLocalHost().getHostAddress());
            if (server == null) {
                server = new PumaServerEntity();
                server.setName(InetAddress.getLocalHost().getHostName());
                server.setHost(InetAddress.getLocalHost().getHostAddress());
                server.setPort(4040);
                create(server);
            } else {
                server.setUpdateTime(new Date());
                update(server);
            }
        } catch (UnknownHostException e) {
            Cat.logError(e);
        }
    }

    @Override
    public void create(PumaServerEntity pumaServer) {
        pumaServerDao.insert(pumaServer);
        //todo: insert other
    }

    @Override
    public void update(PumaServerEntity pumaServer) {
        pumaServerDao.update(pumaServer);
        //todo: update other
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
