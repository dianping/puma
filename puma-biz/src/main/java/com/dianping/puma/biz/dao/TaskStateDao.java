package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.PumaTaskStateEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Dozer @ 7/9/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface TaskStateDao {

    List<PumaTaskStateEntity> findByTaskNameAndTaskType(@Param("taskName") String taskName, @Param("taskType") String taskType);

    List<PumaTaskStateEntity> findByTaskNameAndServerNameAndTaskType(@Param("taskName") String taskName, @Param("serverName") String serverName, @Param("taskType") String taskType);

    int insert(PumaTaskStateEntity entity);

    int update(PumaTaskStateEntity entity);
}
