package com.dianping.puma.biz.service;

import com.dianping.puma.biz.entity.PumaTaskEntity;
import com.dianping.puma.biz.entity.old.PumaTask;

import java.util.List;

public interface PumaTaskService {

    PumaTaskEntity find(int id);

    PumaTaskEntity find(String name);

    List<PumaTaskEntity> findBySrcDBInstanceName(String srcDBInstanceName);

    List<PumaTaskEntity> findByPumaServerName(String pumaServerName);

    List<PumaTaskEntity> findAll();

    long count();

    List<PumaTaskEntity> findByPage(int page, int pageSize);

    void create(PumaTaskEntity pumaTask);

    void update(PumaTaskEntity pumaTask);

    void remove(String name);

    void remove(int id);
}
