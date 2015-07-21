package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.dao.PumaServerDao;
import com.dianping.puma.biz.dao.PumaTaskServerDao;
import com.dianping.puma.biz.dao.PumaTaskTargetDao;
import com.dianping.puma.biz.entity.PumaServerEntity;
import com.dianping.puma.biz.entity.PumaTaskServerEntity;
import com.dianping.puma.biz.entity.PumaTaskTargetEntity;
import com.dianping.puma.biz.service.PumaServerService;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("pumaServerService")
public class PumaServerServiceImpl implements PumaServerService {

    @Autowired
    PumaServerDao pumaServerDao;

    @Autowired
    PumaTaskServerDao pumaTaskServerDao;

    @Autowired
    PumaTaskTargetDao pumaTaskTargetDao;

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
    public List<PumaServerEntity> findByTaskId(int taskId) {
        List<PumaTaskServerEntity> pumaTaskServers = pumaTaskServerDao.findByTaskId(taskId);

        List<PumaServerEntity> pumaServers = new ArrayList<PumaServerEntity>();
        for (PumaTaskServerEntity pumaTaskServer: pumaTaskServers) {
            pumaServers.add(findById(pumaTaskServer.getServerId()));
        }

        return pumaServers;
    }

    @Override
    public List<PumaServerEntity> findByDatabaseAndTables(String database, List<String> tables) {
        List<Integer> taskIds = null;

        for (String table: tables) {
            List<PumaTaskTargetEntity> pumaTaskTargets = pumaTaskTargetDao.findByDatabaseAndTable(database, table);
            List<Integer> tempTaskIds = Lists.transform(pumaTaskTargets, new Function<PumaTaskTargetEntity, Integer>() {
                @Override
                public Integer apply(PumaTaskTargetEntity pumaTaskTargetEntity) {
                    return pumaTaskTargetEntity.getTaskId();
                }
            });

            if (taskIds == null) {
                taskIds = tempTaskIds;
            } else {
                taskIds.retainAll(tempTaskIds);
            }
        }

        List<PumaServerEntity> pumaServers = new ArrayList<PumaServerEntity>();
        if (taskIds != null) {
            for (int taskId: taskIds) {
                pumaServers.addAll(this.findByTaskId(taskId));
            }
        }

        return pumaServers;
    }

    @Override
    public List<PumaServerEntity> findAll() {
        return pumaServerDao.findAll();
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
            server.setGmtUpdate(new Date());
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
