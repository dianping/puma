package com.dianping.puma.instance;

/**
 * Dozer @ 8/7/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface InstanceManager {

    void init();

    void register(String clusterName,InstanceChangedObserver event);

    boolean unregister(String clusterName,InstanceChangedObserver event);
}
