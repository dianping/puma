package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.CheckTaskEntity;

import java.util.List;

public interface CheckTaskDao {

	public CheckTaskEntity findById(int id);

	public List<CheckTaskEntity> findAll();

	public int create(CheckTaskEntity checkTaskEntity);

	public int update(CheckTaskEntity checkTaskEntity);
}
