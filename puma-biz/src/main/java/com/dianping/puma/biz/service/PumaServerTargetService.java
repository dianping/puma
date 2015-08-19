package com.dianping.puma.biz.service;

import com.dianping.puma.biz.entity.PumaServerTargetEntity;

import java.util.List;

public interface PumaServerTargetService {

	public PumaServerTargetEntity findById(int id);

	public List<PumaServerTargetEntity> findByTargetId(int targetId);

	public int create(PumaServerTargetEntity entity);

	public int update(PumaServerTargetEntity entity);

	public int delete(int id);
}
