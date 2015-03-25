package com.dianping.puma.core.service;

import com.dianping.puma.core.entity.DstDBInstance;
import com.dianping.puma.core.entity.SrcDBInstance;

import java.util.List;

public interface SrcDBInstanceService {

	SrcDBInstance find(String name);

    List<SrcDBInstance> findByIp(String ip);

    List<SrcDBInstance> findAll();

	void create(SrcDBInstance srcDBInstance);

	void update(SrcDBInstance srcDBInstance);

	void remove(String name);
}
