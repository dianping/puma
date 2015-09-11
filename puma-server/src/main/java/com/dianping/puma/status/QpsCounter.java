package com.dianping.puma.status;

import com.google.common.base.Preconditions;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Dozer @ 7/22/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class QpsCounter {
    private final int maxSecond;

    private final static int SPLIT = 100;

    private transient final ConcurrentHashMap<Long, AtomicLong> qps = new ConcurrentHashMap<Long, AtomicLong>();

    public QpsCounter() {
        this(1);
    }

    /**
     * 设置最大统计时间
     *
     * @param maxSecond 最大统计时间，如果设置成10秒，那么后续只能得到平均10秒的QPS
     */
    public QpsCounter(int maxSecond) {
        Preconditions.checkArgument(maxSecond > 0, "should > 0");
        this.maxSecond = maxSecond;
    }

    public long get() {
        return get(1);
    }

    public long get(int averageQpsSecond) {
        Preconditions.checkArgument(averageQpsSecond > 0, "averageQpsSecond must > 0");
        Preconditions.checkArgument(averageQpsSecond <= maxSecond, "averageQpsSecond must <= maxSecond");

        long count = 0;
        Iterator<Map.Entry<Long, AtomicLong>> iterator = qps.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, AtomicLong> next = iterator.next();
            if (next.getKey() * 100 + maxSecond * 1000 < System.currentTimeMillis()) {
                iterator.remove();
            }
            if (next.getKey() * 100 + averageQpsSecond * 1000 >= System.currentTimeMillis()) {
                count += next.getValue().get();
            }
        }
        return count / averageQpsSecond;
    }

    public void add(long size) {
        AtomicLong counter = new AtomicLong();
        AtomicLong oldCounter = qps.putIfAbsent(System.currentTimeMillis() / SPLIT, counter);
        if (oldCounter != null) {
            oldCounter.addAndGet(size);
        } else {
            counter.addAndGet(size);
        }
    }

    public void increase() {
        AtomicLong counter = new AtomicLong();
        AtomicLong oldCounter = qps.putIfAbsent(System.currentTimeMillis() / SPLIT, counter);
        if (oldCounter != null) {
            oldCounter.incrementAndGet();
        } else {
            counter.incrementAndGet();
        }
    }
}
