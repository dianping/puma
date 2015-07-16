package com.dianping.puma.biz.service;



import com.dianping.puma.biz.entity.PumaTaskStateEntity;

import java.util.List;

/**
 * Dozer @ 7/8/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface PumaTaskStateService {

    List<PumaTaskStateEntity> find(String name);

    PumaTaskStateEntity find(String name, String serverName);

    List<PumaTaskStateEntity> findByServerName(String serverName);

    void createOrUpdate(PumaTaskStateEntity state);
}