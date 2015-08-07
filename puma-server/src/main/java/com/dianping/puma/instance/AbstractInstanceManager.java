package com.dianping.puma.instance;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Dozer @ 8/7/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public abstract class AbstractInstanceManager implements InstanceManager {

    protected ConcurrentHashMap<String, Set<InstanceChangedObserver>> observers = new ConcurrentHashMap<String, Set<InstanceChangedObserver>>();

    protected static final ExecutorService executorService = Executors.newCachedThreadPool();

    protected void onEvent(final InstanceChangedEvent event) {
        Set<InstanceChangedObserver> observer = observers.get(event.getClusterName());
        if (observer == null) {
            return;
        } else {
            for (final InstanceChangedObserver obs : observer) {
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        obs.onEvent(event);
                    }
                });
            }
        }
    }

    @Override
    public void register(String clusterName, InstanceChangedObserver event) {
        Set<InstanceChangedObserver> observer = Collections.newSetFromMap(new ConcurrentHashMap<InstanceChangedObserver, Boolean>());
        observer = observers.putIfAbsent(clusterName, observer);
        observer.add(event);
    }

    @Override
    public boolean unregister(String clusterName, InstanceChangedObserver event) {
        Set<InstanceChangedObserver> observer = observers.get(clusterName);
        if (observer == null) {
            return false;
        } else {
            return observer.remove(event);
        }
    }
}
