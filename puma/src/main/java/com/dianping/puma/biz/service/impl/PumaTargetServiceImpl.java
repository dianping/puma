package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.dao.PumaServerDao;
import com.dianping.puma.biz.dao.PumaServerTargetDao;
import com.dianping.puma.biz.dao.PumaTargetDao;
import com.dianping.puma.biz.entity.PumaServerEntity;
import com.dianping.puma.biz.entity.PumaServerTargetEntity;
import com.dianping.puma.biz.entity.PumaTargetEntity;
import com.dianping.puma.biz.service.PumaTargetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PumaTargetServiceImpl implements PumaTargetService {

    @Autowired
    PumaTargetDao pumaTargetDao;

    @Autowired
    PumaServerTargetDao pumaServerTargetDao;

    @Autowired
    PumaServerDao pumaServerDao;

    @Override
    public List<PumaTargetEntity> findByDatabase(String database) {
        return pumaTargetDao.findByDatabase(database);
    }

    @Override
    public List<PumaTargetEntity> findByHost(String host) {
        PumaServerEntity pumaServer = pumaServerDao.findByHost(host);
        List<PumaServerTargetEntity> pumaServerTargets = pumaServerTargetDao.findByServerName(pumaServer.getName());

        List<PumaTargetEntity> result = new ArrayList<PumaTargetEntity>();
        for (PumaServerTargetEntity serverTarget : pumaServerTargets) {
            String targetDb = serverTarget.getTargetDb();
            List<PumaTargetEntity> pumaTarget = pumaTargetDao.findByDatabase(targetDb);
            for (PumaTargetEntity target : pumaTarget) {
                target.setBeginTime(serverTarget.getBeginTime());
            }
            result.addAll(pumaTarget);
        }
        return result;
    }

    @Override
    public List<PumaTargetEntity> findAll() {
        return pumaTargetDao.findAll();
    }

    @Override
    public int create(PumaTargetEntity entity) {
        return pumaTargetDao.insert(entity);
    }

    @Override
    public int remove(int id) {
        return pumaTargetDao.delete(id);
    }
}
