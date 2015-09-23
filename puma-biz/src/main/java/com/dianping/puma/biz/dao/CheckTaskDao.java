package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.CheckTaskEntity;

import java.util.List;

public interface CheckTaskDao {

    CheckTaskEntity findById(int id);

    List<CheckTaskEntity> findRunnable();

    int create(CheckTaskEntity checkTaskEntity);

    void cleanUp();

    int unlock(CheckTaskEntity checkTaskEntity);

    int tryLock(CheckTaskEntity checkTaskEntity);

    int deleteByTaskName(String taskName);
}
