package com.dianping.puma.biz.service;

import com.dianping.puma.biz.entity.CheckTaskEntity;

import java.util.List;

public interface CheckTaskService {

	public CheckTaskEntity findById(int id);

	public List<CheckTaskEntity> findAll();

	public int update(CheckTaskEntity checkTaskEntity);
}
