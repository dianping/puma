package com.dianping.puma.biz.service;

import com.dianping.puma.biz.entity.PumaTargetEntity;

import java.util.List;

public interface PumaTargetService {

    List<PumaTargetEntity> findByDatabase(String database);

    List<PumaTargetEntity> findByHost(String host);

    List<PumaTargetEntity> findAll();

    int create(PumaTargetEntity entity);

    int remove(int id);
}
