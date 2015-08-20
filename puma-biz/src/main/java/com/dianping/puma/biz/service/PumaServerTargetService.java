package com.dianping.puma.biz.service;

import com.dianping.puma.biz.entity.PumaServerTargetEntity;

import java.util.List;

public interface PumaServerTargetService {

	public PumaServerTargetEntity findById(int id);

	public List<PumaServerTargetEntity> findByTargetId(int targetId);

	public List<PumaServerTargetEntity> findByDatabase(String database);

	public List<PumaServerTargetEntity> findByServerId(int serverId);

	public List<PumaServerTargetEntity> findByHost(String host);

	public int createOrUpdate(PumaServerTargetEntity entity);

	public int create(PumaServerTargetEntity entity);

	public int update(PumaServerTargetEntity entity);

	public int remove(int id);
}
