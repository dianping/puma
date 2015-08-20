package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.PumaTargetEntity;

import java.util.List;

public interface PumaTargetDao {

	public PumaTargetEntity findById(int id);

	public PumaTargetEntity findByDatabase(String database);

	public List<PumaTargetEntity> findAll();

	public int insert(PumaTargetEntity entity);

	public int update(PumaTargetEntity entity);

	public int delete(int id);
}
