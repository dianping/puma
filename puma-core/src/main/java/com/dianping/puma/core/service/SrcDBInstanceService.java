package com.dianping.puma.core.service;

import com.dianping.puma.core.entity.DstDBInstance;
import com.dianping.puma.core.entity.SrcDBInstance;

import java.util.List;

public interface SrcDBInstanceService {

	SrcDBInstance find(String id);

	SrcDBInstance findByName(String name);

    List<SrcDBInstance> findByIp(String ip);

    List<SrcDBInstance> findAll();

	void create(SrcDBInstance entity);

	void update(SrcDBInstance entity);

	void remove(String id);
}
