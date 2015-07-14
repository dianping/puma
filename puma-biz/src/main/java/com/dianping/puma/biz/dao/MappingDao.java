package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.MappingEntity;

public interface MappingDao {

	MappingEntity find(int id);

	int insert(MappingEntity entity);

	int update(MappingEntity entity);

	int delete(MappingEntity entity);
}
