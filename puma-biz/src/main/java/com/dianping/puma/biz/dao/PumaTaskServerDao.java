package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.PumaTaskServerEntity;

import java.util.List;

/**
 * Dozer @ 7/10/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface PumaTaskServerDao {

    List<PumaTaskServerEntity> findByTaskId(int taskId);

    int deleteByTaskId(int taskId);

    int insert(PumaTaskServerEntity entity);
}
