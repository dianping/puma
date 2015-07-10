package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.PumaTaskDbEntity;
import com.dianping.puma.biz.entity.PumaTaskServerEntity;
import com.dianping.puma.biz.entity.SrcDbEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Dozer @ 7/10/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface PumaTaskServerDao {

    List<PumaTaskServerEntity> findByTaskId(@Param(value = "taskId") int taskId);

    int delete(@Param(value = "id") int id);

    int insert(PumaTaskServerEntity entity);

}
