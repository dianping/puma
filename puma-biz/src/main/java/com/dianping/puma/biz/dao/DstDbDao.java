package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.DstDbEntity;

public interface DstDbDao {

	DstDbEntity find(int id);

	int insert(DstDbEntity entity);

	int update(DstDbEntity entity);

	int delete(DstDbEntity entity);
}
