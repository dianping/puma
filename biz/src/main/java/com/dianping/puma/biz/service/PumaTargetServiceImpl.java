package com.dianping.puma.biz.service;

import com.dianping.puma.common.convert.Converter;
import com.dianping.puma.biz.dao.PumaServerDao;
import com.dianping.puma.biz.dao.PumaServerTargetDao;
import com.dianping.puma.biz.dao.PumaTargetDao;
import com.dianping.puma.biz.entity.PumaServerEntity;
import com.dianping.puma.biz.entity.PumaServerTargetEntity;
import com.dianping.puma.biz.entity.PumaTargetEntity;
import com.dianping.puma.common.model.PumaTarget;
import com.dianping.puma.common.service.PumaTargetService;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.List;

public class PumaTargetServiceImpl implements PumaTargetService {

    private Converter converter;

    private PumaTargetDao pumaTargetDao;

    private PumaServerTargetDao pumaServerTargetDao;

    private PumaServerDao pumaServerDao;

    @Override
    public List<PumaTarget> findByDatabase(String database) {
        return FluentIterable
                .from(pumaTargetDao.findByDatabase(database))
                .transform(new Function<PumaTargetEntity, PumaTarget>() {
                    @Override
                    public PumaTarget apply(PumaTargetEntity entity) {
                        return converter.convert(entity, PumaTarget.class);
                    }
                }).toList();
    }

    @Override
    public List<PumaTarget> findByHost(String host) {
        PumaServerEntity pumaServer = pumaServerDao.findByHost(host);
        List<PumaServerTargetEntity> pumaServerTargets = pumaServerTargetDao.findByServerName(pumaServer.getName());

        List<PumaTarget> result = new ArrayList<PumaTarget>();
        for (PumaServerTargetEntity serverTarget : pumaServerTargets) {
            String targetDb = serverTarget.getTargetDb();
            List<PumaTarget> pumaTarget = findByDatabase(targetDb);
            for (PumaTarget target : pumaTarget) {
                target.setBeginTime(serverTarget.getBeginTime());
            }
            result.addAll(pumaTarget);
        }
        return result;
    }

    @Override
    public List<PumaTarget> findAll() {
        return FluentIterable
                .from(pumaTargetDao.findAll())
                .transform(new Function<PumaTargetEntity, PumaTarget>() {
                    @Override
                    public PumaTarget apply(PumaTargetEntity entity) {
                        return converter.convert(entity, PumaTarget.class);
                    }
                }).toList();
    }

    @Override
    public int create(PumaTarget pumaTarget) {
        PumaTargetEntity entity = converter.convert(pumaTarget, PumaTargetEntity.class);
        return pumaTargetDao.insert(entity);
    }

    @Override
    public int remove(int id) {
        return pumaTargetDao.delete(id);
    }

    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    public void setPumaTargetDao(PumaTargetDao pumaTargetDao) {
        this.pumaTargetDao = pumaTargetDao;
    }

    public void setPumaServerTargetDao(PumaServerTargetDao pumaServerTargetDao) {
        this.pumaServerTargetDao = pumaServerTargetDao;
    }

    public void setPumaServerDao(PumaServerDao pumaServerDao) {
        this.pumaServerDao = pumaServerDao;
    }
}
