package com.dianping.puma.core.dao;

import java.util.List;

import com.dianping.puma.core.entity.PumaClientInfoEntity;

public interface PumaClientInfoDao {

	public PumaClientInfoEntity find(String id);
	
	public List<PumaClientInfoEntity> findAll();
	
	void create(PumaClientInfoEntity entity);

	void update(PumaClientInfoEntity entity);

	void remove(String id);
}
