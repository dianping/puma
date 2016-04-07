package com.dianping.puma.biz.service;

import com.dianping.puma.common.convert.Converter;
import com.dianping.puma.biz.dao.PumaServerDao;
import com.dianping.puma.biz.entity.PumaServerEntity;
import com.dianping.puma.biz.util.IPUtils;
import com.dianping.puma.common.model.PumaServer;
import com.dianping.puma.common.service.PumaServerService;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PumaServerServiceImpl implements PumaServerService {

    private Converter converter;

    private PumaServerDao pumaServerDao;

    @Override
    public PumaServer find(String name) {
        PumaServerEntity entity = pumaServerDao.findByName(name);
        return converter.convert(entity, PumaServer.class);
    }

    @Override
    public PumaServer findById(int id) {
        PumaServerEntity entity = pumaServerDao.findById(id);
        return converter.convert(entity, PumaServer.class);
    }

    @Override
    public PumaServer findByHost(String host) {
        PumaServerEntity entity = pumaServerDao.findByHost(host);
        return converter.convert(entity, PumaServer.class);
    }

    @Override
    public List<PumaServer> findOnCurrentServer() {
        List<PumaServer> result = new ArrayList<PumaServer>();
        for (String host : IPUtils.getNoLoopbackIP4Addresses()) {
            PumaServer pumaServer = findByHost(host);
            if (pumaServer != null) {
                result.add(pumaServer);
            }
        }
        return result;
    }

    @Override
    public List<PumaServer> findAll() {
        return FluentIterable
                .from(pumaServerDao.findAll())
                .transform(new Function<PumaServerEntity, PumaServer>() {
                    @Override
                    public PumaServer apply(PumaServerEntity entity) {
                        return converter.convert(entity, PumaServer.class);
                    }
                }).toList();
    }

    @Override
    public List<PumaServer> findAllAlive() {
        return FluentIterable
                .from(pumaServerDao.findAllAlive())
                .transform(new Function<PumaServerEntity, PumaServer>() {
                    @Override
                    public PumaServer apply(PumaServerEntity entity) {
                        return converter.convert(entity, PumaServer.class);
                    }
                }).toList();
    }

    @Override
    public List<PumaServer> findByPage(int page, int pageSize) {
        return FluentIterable
                .from(pumaServerDao.findByPage((page - 1) * pageSize, pageSize))
                .transform(new Function<PumaServerEntity, PumaServer>() {
                    @Override
                    public PumaServer apply(PumaServerEntity entity) {
                        return converter.convert(entity, PumaServer.class);
                    }
                }).toList();
    }

    @Override
    public long count() {
        return pumaServerDao.count();
    }

    @Override
    public void registerByHost(String host) {
        PumaServer pumaServer = findByHost(host);
        if (pumaServer == null) {
            pumaServer = new PumaServer();
            pumaServer.setName(host);
            pumaServer.setHost(host);
            pumaServer.setPort(4040);
            create(pumaServer);
        } else {
            pumaServer.setUpdateTime(new Date());
            update(pumaServer);
        }
    }

    @Override
    public void create(PumaServer pumaServer) {
        PumaServerEntity entity = converter.convert(pumaServer, PumaServerEntity.class);
        pumaServerDao.insert(entity);
    }

    @Override
    public void update(PumaServer pumaServer) {
        PumaServerEntity entity = converter.convert(pumaServer, PumaServerEntity.class);
        pumaServerDao.update(entity);
    }

    @Override
    public void remove(String name) {
        pumaServerDao.deleteByName(name);
    }

    @Override
    public void remove(int id) {
        pumaServerDao.delete(id);
    }

    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    public void setPumaServerDao(PumaServerDao pumaServerDao) {
        this.pumaServerDao = pumaServerDao;
    }
}
