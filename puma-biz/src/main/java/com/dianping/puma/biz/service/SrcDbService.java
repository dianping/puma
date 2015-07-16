package com.dianping.puma.biz.service;

import java.util.List;

import com.dianping.puma.biz.entity.SrcDbEntity;

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
