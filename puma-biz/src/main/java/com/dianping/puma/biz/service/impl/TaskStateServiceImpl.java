package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.entity.TaskStateEntity;
import com.dianping.puma.biz.service.TaskStateService;

import java.util.Date;
import java.util.List;

/**
 * Dozer @ 7/9/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public abstract class TaskStateServiceImpl implements TaskStateService {
    protected abstract String getTypeName();

    @Override
    public List<TaskStateEntity> find(String name) {
        return null;
    }

    @Override
    public TaskStateEntity find(String name, String serverName) {
        return null;
    }

    @Override
    public void createOrUpdate(TaskStateEntity state) {
        state.setTaskType(getTypeName());
        state.setGmtUpdate(new Date());


    }
}
