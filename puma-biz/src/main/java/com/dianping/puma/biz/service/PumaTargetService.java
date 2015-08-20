package com.dianping.puma.biz.service;

import com.dianping.puma.biz.entity.PumaTargetEntity;

import java.util.List;

public interface PumaTargetService {

	public PumaTargetEntity findById(int id);

	public PumaTargetEntity findByDatabase(String database);

	public List<PumaTargetEntity> findByHost(String host);

	public List<PumaTargetEntity> findAll();

	public int createOrUpdate(PumaTargetEntity entity);

	public int create(PumaTargetEntity entity);

	public int update(PumaTargetEntity entity);

	public int remove(int id);
}
