package com.dianping.puma.core.dao;

import com.dianping.puma.core.entity.SrcDBInstance;

import java.util.List;

public interface SrcDBInstanceDao {

	SrcDBInstance find(long id);
	
	SrcDBInstance find(String name);

    List<SrcDBInstance> findAll();

    List<SrcDBInstance> findByIp(String ip);

	long count();

	List<SrcDBInstance> findByPage(int page, int pageSize);
	
    void create(SrcDBInstance entity);

    void update(SrcDBInstance entity);

    void remove(String name);
    
    void remove(long id);
}
