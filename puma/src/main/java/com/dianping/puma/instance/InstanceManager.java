package com.dianping.puma.instance;

import com.dianping.puma.biz.entity.SrcDbEntity;

import java.util.Set;

/**
 * Dozer @ 8/7/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface InstanceManager {

    void init();

    Set<SrcDbEntity> getUrlByCluster(String clusterName);

    String getClusterByDb(String db);

}
