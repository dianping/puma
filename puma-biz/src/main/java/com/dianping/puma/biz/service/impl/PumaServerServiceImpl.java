package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.entity.PumaServerEntity;
import com.dianping.puma.biz.olddao.PumaServerDao;
import com.dianping.puma.biz.service.PumaServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("pumaServerService")
public class PumaServerServiceImpl implements PumaServerService {

    @Autowired
    PumaServerDao pumaServerDao;

    @Override
    public PumaServerEntity find(String name) {
        return null;
//        return pumaServerDao.find(name);
    }

    @Override
    public PumaServerEntity find(long id) {
        return null;
//        return pumaServerDao.find(id);
    }

    public PumaServerEntity findByHost(String host) {
        return null;
//        return pumaServerDao.findByHost(host);
    }

    @Override
    public List<PumaServerEntity> findAll() {
        return null;
//        return pumaServerDao.findAll();
    }

    @Override
    public long count() {
        return pumaServerDao.count();
    }

    @Override
    public List<PumaServerEntity> findByPage(int page, int pageSize) {
        return null;
//        return pumaServerDao.findByPage(page, pageSize);
    }

    @Override
    public void heartBeat() {
//        try {
//            PumaServer server = findByHost(InetAddress.getLocalHost().getHostAddress());
//            if (server == null) {
//                server = new PumaServer();
//                server.setName(InetAddress.getLocalHost().getHostName());
//                server.setHost(InetAddress.getLocalHost().getHostAddress());
//                server.setPort(4040);
//                create(server);
//            } else {
//                server.upgrade();
//                update(server);
//            }
//        } catch (UnknownHostException e) {
//            Cat.logError(e);
//        }
    }

    @Override
    public void create(PumaServerEntity pumaServer) {
//        pumaServerDao.create(pumaServer);
    }

    @Override
    public void update(PumaServerEntity pumaServer) {
//        pumaServerDao.update(pumaServer);
    }

    @Override
    public void remove(String name) {
//        pumaServerDao.remove(name);
    }

    @Override
    public void remove(int id) {
//        pumaServerDao.remove(id);
    }
}
