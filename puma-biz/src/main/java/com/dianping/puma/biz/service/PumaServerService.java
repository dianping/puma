package com.dianping.puma.biz.service;

import com.dianping.puma.biz.entity.PumaServerEntity;
import com.dianping.puma.biz.entity.old.PumaServer;

import java.util.List;

public interface PumaServerService {

    PumaServerEntity find(int id);

    PumaServerEntity find(String name);

    PumaServerEntity findByHost(String host);

    List<PumaServerEntity> findAll();

    List<PumaServerEntity> findByPage(int page, int pageSize);

    long count();

    void registerByHost(String host);

    void heartBeat();

    void create(PumaServerEntity pumaServer);

    void update(PumaServerEntity pumaServer);

    void remove(String name);

    void remove(int id);
}
