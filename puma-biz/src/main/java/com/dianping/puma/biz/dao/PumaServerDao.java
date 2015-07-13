package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.PumaServerEntity;

import java.util.List;

public interface PumaServerDao {

    PumaServerEntity findById(int id);

    PumaServerEntity findByName(String name);

    PumaServerEntity findByHost(String host);

    List<PumaServerEntity> findAll();

    List<PumaServerEntity> findByPage(int offset, int limit);

    long count();

    int insert(PumaServerEntity entity);

    int update(PumaServerEntity entity);

    int delete(int id);

    int deleteByName(String name);
}
