package com.dianping.puma.biz.service;

import com.dianping.puma.biz.entity.DstDbEntity;
import com.dianping.puma.biz.entity.SrcDbEntity;

import java.util.List;

public interface DstDbService {

	DstDbEntity find(int id);

	DstDbEntity find(String name);

	List<SrcDbEntity> findAll();

	void create(SrcDbEntity srcDBInstance);

	void update(SrcDbEntity srcDBInstance);

	void remove(String name);

	void remove(int id);
}
