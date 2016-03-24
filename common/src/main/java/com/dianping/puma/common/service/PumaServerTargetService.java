package com.dianping.puma.common.service;

import com.dianping.puma.common.model.PumaServerTarget;

import java.util.List;

public interface PumaServerTargetService {

    List<PumaServerTarget> findByDatabase(String database);

    List<PumaServerTarget> findByServerHost(String host);

    int create(PumaServerTarget entity);

    int replace(PumaServerTarget entity);

    int update(PumaServerTarget entity);

    int remove(int id);
}
