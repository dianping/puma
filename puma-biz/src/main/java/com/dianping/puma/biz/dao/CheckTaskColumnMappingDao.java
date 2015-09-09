package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.CheckTaskColumnMappingEntity;

import java.util.List;

public interface CheckTaskColumnMappingDao {

	CheckTaskColumnMappingEntity findById(int id);

	List<CheckTaskColumnMappingEntity> findByTaskId(int taskId);
}
