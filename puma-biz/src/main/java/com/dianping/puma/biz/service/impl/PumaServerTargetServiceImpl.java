package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.dao.PumaServerDao;
import com.dianping.puma.biz.dao.PumaServerTargetDao;
import com.dianping.puma.biz.dao.PumaTargetDao;
import com.dianping.puma.biz.entity.PumaServerEntity;
import com.dianping.puma.biz.entity.PumaServerTargetEntity;
import com.dianping.puma.biz.entity.PumaTargetEntity;
import com.dianping.puma.biz.entity.old.PumaServer;
import com.dianping.puma.biz.service.PumaServerTargetService;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class PumaServerTargetServiceImpl implements PumaServerTargetService {

    @Autowired
    PumaServerTargetDao pumaServerTargetDao;

    @Autowired
    PumaTargetDao pumaTargetDao;

    @Autowired
    PumaServerDao pumaServerDao;

    @Override
    public List<PumaServerTargetEntity> findByDatabase(String database) {
        List<PumaTargetEntity> pumaTargets = pumaTargetDao.findByDatabase(database);

        List<String> tables = Lists.transform(pumaTargets, new Function<PumaTargetEntity, String>() {
            @Override
            public String apply(PumaTargetEntity pumaTarget) {
                return pumaTarget.getTable();
            }
        });

        List<PumaServerTargetEntity> pumaServerTargets = pumaServerTargetDao.findByDatabase(database);
        for (PumaServerTargetEntity pumaServerTarget: pumaServerTargets) {
            pumaServerTarget.setTables(tables);

            String serverName = pumaServerTarget.getServerName();
            PumaServerEntity pumaServer = pumaServerDao.findByName(serverName);
            pumaServerTarget.setServerHost(pumaServer.getHost());
        }

        return pumaServerTargets;
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
    public int replace(PumaServerTargetEntity entity) {
        return pumaServerTargetDao.replace(entity);
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
