package com.dianping.puma.portal.db;

import java.util.Map;
import java.util.Set;

/**
 * Dozer @ 8/7/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface InstanceManager {

    void init();

    Set<String> getUrlByCluster(String clusterName);

    String getClusterByDb(String db);

    Map<String, String> getDbClusterMap();

}
