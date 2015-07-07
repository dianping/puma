package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.PumaServer;

import java.util.List;

public interface PumaServerDao {

	PumaServer find(long id);

	PumaServer find(String name);

	PumaServer findByHost(String host);

	List<PumaServer> findAll();

	long count();

	List<PumaServer> findByPage(int page, int pageSize);

	void create(PumaServer pumaServer);

	void update(PumaServer pumaServer);

	void remove(String name);

	void remove(long id);
}
