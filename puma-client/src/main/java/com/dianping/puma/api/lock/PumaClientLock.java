package com.dianping.puma.api.lock;

import java.util.concurrent.TimeUnit;

/**
 * Dozer @ 2015-10
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface PumaClientLock {

    void lock() throws Exception;

    boolean lock(long time, TimeUnit timeUnit) throws Exception;

    void unlock() throws Exception;

}
