package com.dianping.puma.api.cleanup;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dozer @ 2015-10
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public final class CleanUpHelper {

    private static volatile boolean started = false;

    private static final Thread cleanUpThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Reference target = referenceQueue.poll();
                    if (target != null) {
                        CleanUp cleanUp = maps.remove(target);
                        if (cleanUp != null) {
                            cleanUp.cleanUp();
                            continue;
                        }
                    }
                } catch (RuntimeException ignore) {
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    });

    private static final Map<Reference<Object>, CleanUp> maps = new ConcurrentHashMap<Reference<Object>, CleanUp>();

    private static final ReferenceQueue<Object> referenceQueue = new ReferenceQueue<Object>();

    public static void register(Object watcher, CleanUp cleanUp) {
        init();
        maps.put(new PhantomReference<Object>(watcher, referenceQueue), cleanUp);
    }

    private static void init() {
        if (!started) {
            synchronized (CleanUpHelper.class) {
                if (!started) {
                    cleanUpThread.setName("CleanUpThread");
                    cleanUpThread.setDaemon(true);
                    cleanUpThread.start();
                    started = true;
                }
            }
        }
    }
}
