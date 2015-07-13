package com.dianping.puma.biz.service;

import com.dianping.puma.biz.entity.SrcDbEntity;
import com.dianping.puma.biz.entity.old.SrcDBInstance;

import java.util.List;

public interface SrcDBInstanceService {
	
	SrcDbEntity find(long id);

	SrcDbEntity find(String name);

    List<SrcDbEntity> findByIp(String ip);

    List<SrcDbEntity> findAll();

    long count();

	List<SrcDbEntity> findByPage(int page, int pageSize);
	
	void create(SrcDbEntity srcDBInstance);

	void update(SrcDbEntity srcDBInstance);

	void remove(String name);
	
	void remove(int id);
}
