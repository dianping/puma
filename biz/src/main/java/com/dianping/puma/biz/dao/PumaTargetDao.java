package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.PumaTargetEntity;

import java.util.List;

public interface PumaTargetDao {

    PumaTargetEntity findById(int id);

    List<PumaTargetEntity> findByDatabase(String database);

    List<PumaTargetEntity> findAll();

    int insert(PumaTargetEntity entity);

    int replace(PumaTargetEntity entity);

    int delete(int id);
}
