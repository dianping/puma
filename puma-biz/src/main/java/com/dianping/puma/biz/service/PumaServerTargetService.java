package com.dianping.puma.biz.service;

import com.dianping.puma.biz.entity.PumaServerTargetEntity;

import java.util.List;

public interface PumaServerTargetService {

    List<PumaServerTargetEntity> findByDatabase(String database);

    List<PumaServerTargetEntity> findByServerHost(String host);

    int create(PumaServerTargetEntity entity);

    int replace(PumaServerTargetEntity entity);

    int update(PumaServerTargetEntity entity);

    int remove(int id);
}
