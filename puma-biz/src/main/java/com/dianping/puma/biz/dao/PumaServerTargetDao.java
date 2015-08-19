package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.PumaServerTargetEntity;

import java.util.List;

public interface PumaServerTargetDao {

	public PumaServerTargetEntity findById(int id);

	public List<PumaServerTargetEntity> findByServerId(int serverId);

	public int insert(PumaServerTargetEntity entity);

	public int update(PumaServerTargetEntity entity);

	public int delete(int id);
}
