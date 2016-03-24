package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.PumaServerTargetEntity;

import java.util.List;

public interface PumaServerTargetDao {

    List<PumaServerTargetEntity> findByDatabase(String targetDb);

    List<PumaServerTargetEntity> findByServerName(String serverName);

    int insert(PumaServerTargetEntity entity);

    int replace(PumaServerTargetEntity entity);

    int update(PumaServerTargetEntity entity);

    int delete(int id);
}
