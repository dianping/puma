package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.CheckTaskEntity;

import java.util.List;

public interface CheckTaskDao {

    CheckTaskEntity findById(int id);

    List<CheckTaskEntity> findAll();

    int create(CheckTaskEntity checkTaskEntity);

    int update(CheckTaskEntity checkTaskEntity);

    int deleteByTaskName(String taskName);
}
