package com.dianping.puma.biz.service;

import com.dianping.puma.common.convert.Converter;
import com.dianping.puma.biz.dao.PumaServerDao;
import com.dianping.puma.biz.dao.PumaServerTargetDao;
import com.dianping.puma.biz.dao.PumaTargetDao;
import com.dianping.puma.biz.entity.PumaServerEntity;
import com.dianping.puma.biz.entity.PumaServerTargetEntity;
import com.dianping.puma.biz.entity.PumaTargetEntity;
import com.dianping.puma.common.model.PumaServerTarget;
import com.dianping.puma.common.service.PumaServerTargetService;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import java.util.List;

public class PumaServerTargetServiceImpl implements PumaServerTargetService {

    private Converter converter;

    private PumaServerTargetDao pumaServerTargetDao;

    private PumaTargetDao pumaTargetDao;

    private PumaServerDao pumaServerDao;

    @Override
    public List<PumaServerTarget> findByDatabase(String database) {
        List<PumaTargetEntity> pumaTargets = pumaTargetDao.findByDatabase(database);

        List<String> tables = Lists.transform(pumaTargets, new Function<PumaTargetEntity, String>() {
            @Override
            public String apply(PumaTargetEntity pumaTarget) {
                return pumaTarget.getTable();
            }
        });

        List<PumaServerTarget> pumaServerTargets = FluentIterable
                .from(pumaServerTargetDao.findByDatabase(database))
                .transform(new Function<PumaServerTargetEntity, PumaServerTarget>() {
                    @Override
                    public PumaServerTarget apply(PumaServerTargetEntity entity) {
                        return converter.convert(entity, PumaServerTarget.class);
                    }
                }).toList();

        for (PumaServerTarget pumaServerTarget: pumaServerTargets) {
            pumaServerTarget.setTables(tables);

            String serverName = pumaServerTarget.getServerName();
            PumaServerEntity pumaServer = pumaServerDao.findByName(serverName);
            pumaServerTarget.setServerHost(pumaServer.getHost());
        }

        return pumaServerTargets;
    }

    @Override
    public List<PumaServerTarget> findByServerHost(String host) {
        PumaServerEntity pumaServer = pumaServerDao.findByHost(host);
        if (pumaServer == null) {
            return Lists.newArrayList();
        }

        String serverName = pumaServer.getName();

        List<PumaServerTarget> pumaServerTargets = FluentIterable
                .from(pumaServerTargetDao.findByServerName(serverName))
                .transform(new Function<PumaServerTargetEntity, PumaServerTarget>() {
                    @Override
                    public PumaServerTarget apply(PumaServerTargetEntity entity) {
                        return converter.convert(entity, PumaServerTarget.class);
                    }
                }).toList();

        for (PumaServerTarget pumaServerTarget: pumaServerTargets) {
            pumaServerTarget.setServerHost(host);

            String database = pumaServerTarget.getTargetDb();
            List<PumaTargetEntity> pumaTargets = pumaTargetDao.findByDatabase(database);
            List<String> tables = Lists.transform(pumaTargets, new Function<PumaTargetEntity, String>() {
                @Override
                public String apply(PumaTargetEntity pumaTarget) {
                    return pumaTarget.getTable();
                }
            });

            pumaServerTarget.setTables(tables);
        }

        return pumaServerTargets;
    }

    @Override
    public int create(PumaServerTarget pumaServerTarget) {
        PumaServerTargetEntity entity = converter.convert(pumaServerTarget, PumaServerTargetEntity.class);
        return pumaServerTargetDao.insert(entity);
    }

    @Override
    public int replace(PumaServerTarget pumaServerTarget) {
        PumaServerTargetEntity entity = converter.convert(pumaServerTarget, PumaServerTargetEntity.class);
        return pumaServerTargetDao.replace(entity);
    }

    @Override
    public int update(PumaServerTarget pumaServerTarget) {
        PumaServerTargetEntity entity = converter.convert(pumaServerTarget, PumaServerTargetEntity.class);
        return pumaServerTargetDao.update(entity);
    }

    @Override
    public int remove(int id) {
        return pumaServerTargetDao.delete(id);
    }

    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    public void setPumaServerTargetDao(PumaServerTargetDao pumaServerTargetDao) {
        this.pumaServerTargetDao = pumaServerTargetDao;
    }

    public void setPumaTargetDao(PumaTargetDao pumaTargetDao) {
        this.pumaTargetDao = pumaTargetDao;
    }

    public void setPumaServerDao(PumaServerDao pumaServerDao) {
        this.pumaServerDao = pumaServerDao;
    }
}
