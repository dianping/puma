package com.dianping.puma.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by xiaotian.li on 16/3/2.
 * Email: lixiaotian07@gmail.com
 */
public class NamedThreadFactory implements ThreadFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(NamedThreadFactory.class);

    private static final String DEFAULT_NAME = "sync-pool";

    private String name;

    private boolean daemon;

    private ThreadGroup group;

    private AtomicInteger threadNumber = new AtomicInteger(0);

    static Thread.UncaughtExceptionHandler uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t,
                                      Throwable e) {
            LOGGER.error("from " + t.getName(), e);
        }
    };

    public NamedThreadFactory(){
        this(DEFAULT_NAME, true);
    }

    public NamedThreadFactory(String name){
        this(name, true);
    }

    public NamedThreadFactory(String name, boolean daemon){
        this.name = name;
        this.daemon = daemon;
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, name + "-" + threadNumber.getAndIncrement(), 0);
        t.setDaemon(daemon);
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }

        t.setUncaughtExceptionHandler(uncaughtExceptionHandler);
        return t;
    }
}
