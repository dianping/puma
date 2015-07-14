package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.PumaTaskEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Dozer @ 7/10/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface PumaTaskDao {

    PumaTaskEntity findByName(String name);

    PumaTaskEntity findById(int id);

    List<PumaTaskEntity> findBySrcDbName(String name);

    List<PumaTaskEntity> findByPumaServerName(String name);

    List<PumaTaskEntity> findAll();

    List<PumaTaskEntity> findByPage(@Param(value = "offset") int offset, @Param(value = "limit") int limit);

    int count();

    int insert(PumaTaskEntity entity);

    int update(PumaTaskEntity entity);

    int delete(int id);

    int deleteByName(String name);
}
