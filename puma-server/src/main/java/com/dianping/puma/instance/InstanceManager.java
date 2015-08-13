package com.dianping.puma.instance;

import java.util.Set;

/**
 * Dozer @ 8/7/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface InstanceManager {

    void init();

    Set<String> getUrlByCluster(String clusterName);

    void register(String clusterName,InstanceChangedObserver event);

    boolean unregister(String clusterName,InstanceChangedObserver event);
}
