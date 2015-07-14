package com.dianping.puma.biz.service.impl;

import com.dianping.cat.Cat;
import com.dianping.puma.biz.olddao.SyncServerDao;
import com.dianping.puma.biz.entity.old.SyncServer;
import com.dianping.puma.biz.service.SyncServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class SyncServerServiceImpl implements SyncServerService {

    @Autowired
    SyncServerDao syncServerDao;

    @Override
    public SyncServer find(String name) {
        return syncServerDao.find(name);
    }

    @Override
    public SyncServer find(long id) {
        return syncServerDao.find(id);
    }

    @Override
    public List<SyncServer> findAll() {
        return syncServerDao.findAll();
    }

    @Override
    public long count() {
        return syncServerDao.count();
    }

    @Override
    public List<SyncServer> findByPage(int page, int pageSize) {
        return syncServerDao.findByPage(page, pageSize);
    }

    @Override
    public void create(SyncServer syncServer) {
        syncServerDao.create(syncServer);
    }

    @Override
    public void update(SyncServer syncServer) {
        syncServerDao.update(syncServer);
    }

    @Override
    public void remove(String name) {
        syncServerDao.remove(name);
    }

    @Override
    public void remove(long id) {
        syncServerDao.remove(id);
    }

    @Override
    public SyncServer findByHost(String host) {
        return syncServerDao.findByHost(host);
    }

    @Override
    public void heartBeat() {
        try {
            SyncServer server = findByHost(InetAddress.getLocalHost().getHostAddress());
            if (server == null) {
                server = new SyncServer();
                server.setName(InetAddress.getLocalHost().getHostName());
                server.setHost(InetAddress.getLocalHost().getHostAddress());
                server.setPort(80);
                create(server);
            } else {
                server.upgrade();
                update(server);
            }
        } catch (UnknownHostException e) {
            Cat.logError(e);
        }
    }
}
