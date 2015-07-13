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

    List<PumaTaskEntity> findByIds(@Param(value = "ids") List<Integer> ids);

    PumaTaskEntity findByName(@Param(value = "name") String name);

    PumaTaskEntity findById(@Param(value = "id") int id);

    int insert(PumaTaskEntity entity);

    int update(PumaTaskEntity entity);
}
