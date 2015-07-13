package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.PumaTaskDbEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Dozer @ 7/10/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface PumaTaskDbDao {

    List<PumaTaskDbEntity> findByTaskId(@Param(value = "taskId") int taskId);

    int delete(@Param(value = "id") int id);

    int insert(PumaTaskDbEntity entity);

}
