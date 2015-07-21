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

    List<PumaTaskTargetEntity> findByTaskId(int taskId);

    List<PumaTaskTargetEntity> findByDatabaseAndTable(
            @Param("database") String database,
            @Param("table") String table);

    int insert(PumaTaskTargetEntity entity);

    int update(PumaTaskTargetEntity entity);

    int deleteByTaskId(int taskId);
}
