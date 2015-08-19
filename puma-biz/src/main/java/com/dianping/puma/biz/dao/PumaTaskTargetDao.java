package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.PumaTargetEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Dozer @ 7/10/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface PumaTaskTargetDao {

    List<PumaTargetEntity> findByServerId(int ServerId);

    List<PumaTargetEntity> findByDatabaseAndTable(
            @Param("database") String database,
            @Param("table") String table);

    int insert(PumaTargetEntity entity);

    int update(PumaTargetEntity entity);

    int delete(int id);
}
