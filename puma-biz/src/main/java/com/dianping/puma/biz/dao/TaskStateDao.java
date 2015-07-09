package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.TaskStateEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Dozer @ 7/9/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface TaskStateDao {

    List<TaskStateEntity> findByTaskNameAndTaskType(@Param("taskName") String taskName, @Param("taskType") String taskType);

    List<TaskStateEntity> findByTaskNameAndServerNameAndTaskType(@Param("taskName") String taskName, @Param("serverName") String serverName, @Param("taskType") String taskType);

    int insert(TaskStateEntity entity);

    int update(TaskStateEntity entity);
}
