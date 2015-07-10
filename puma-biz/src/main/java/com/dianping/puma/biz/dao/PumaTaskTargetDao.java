package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.PumaTaskTargetEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Dozer @ 7/10/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface PumaTaskTargetDao {
    List<PumaTaskTargetEntity> findByTaskId(@Param(value = "taskId") int taskId);

    int insert(PumaTaskTargetEntity entity);

    int update(PumaTaskTargetEntity entity);
}
