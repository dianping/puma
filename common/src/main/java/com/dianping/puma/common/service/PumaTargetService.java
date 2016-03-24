package com.dianping.puma.common.service;

import com.dianping.puma.common.model.PumaTarget;

import java.util.List;

public interface PumaTargetService {

    List<PumaTarget> findByDatabase(String database);

    List<PumaTarget> findByHost(String host);

    List<PumaTarget> findAll();

    int create(PumaTarget entity);

    int remove(int id);
}
