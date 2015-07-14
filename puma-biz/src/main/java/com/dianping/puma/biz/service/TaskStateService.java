package com.dianping.puma.biz.service;



import com.dianping.puma.biz.entity.TaskStateEntity;

import java.util.List;

/**
 * Dozer @ 7/8/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface TaskStateService {

    List<TaskStateEntity> find(String name);

    TaskStateEntity find(String name, String serverName);

    void createOrUpdate(TaskStateEntity state);
}