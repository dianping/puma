package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.PumaTaskDbEntity;

import java.util.List;

/**
 * Dozer @ 7/10/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface PumaTaskDbDao {

    List<PumaTaskDbEntity> findByTaskId(int taskId);

    int deleteByTaskId(int taskId);

    int insert(PumaTaskDbEntity entity);

}
