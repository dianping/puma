package com.dianping.puma.biz.service;

import com.dianping.puma.biz.entity.SrcDbEntity;

import java.util.List;

public interface SrcDbService {

	SrcDbEntity find(int id);

	SrcDbEntity find(String name);

	List<SrcDbEntity> findAll();

	long count();

	List<SrcDbEntity> findByPage(int page, int pageSize);

	void create(SrcDbEntity srcDBInstance);

	void update(SrcDbEntity srcDBInstance);

	void remove(String name);

	void remove(int id);
}
