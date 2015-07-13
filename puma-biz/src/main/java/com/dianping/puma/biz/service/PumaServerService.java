package com.dianping.puma.biz.service;

import com.dianping.puma.biz.entity.PumaServerEntity;
import com.dianping.puma.biz.entity.old.PumaServer;

import java.util.List;

public interface PumaServerService {

    PumaServerEntity find(long id);

    PumaServerEntity find(String name);

    PumaServerEntity findByHost(String host);

    List<PumaServerEntity> findAll();

    long count();

    List<PumaServerEntity> findByPage(int page, int pageSize);

    void heartBeat();

    @Deprecated
    void create(PumaServerEntity pumaServer);

    void update(PumaServerEntity pumaServer);

    void remove(String name);

    void remove(int id);
}
