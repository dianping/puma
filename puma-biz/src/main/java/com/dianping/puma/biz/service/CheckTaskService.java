package com.dianping.puma.biz.service;

import com.dianping.puma.biz.entity.CheckTaskEntity;

import java.util.List;

public interface CheckTaskService {

    CheckTaskEntity findById(int id);

    List<CheckTaskEntity> findRunnable();

    int update(CheckTaskEntity checkTaskEntity);

    int create(CheckTaskEntity checkTaskEntity);

    int deleteByTaskName(String name);
}
