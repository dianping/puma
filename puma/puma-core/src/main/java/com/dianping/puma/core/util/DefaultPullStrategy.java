package com.dianping.puma.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 失败时累加failCnt，sleep(min(delayBase * failCnt, delayUpperbound))，成功时清零failCnt
 * 
 * @author marsqing
 */
public class DefaultPullStrategy implements PullStrategy {

    private static Logger log = LoggerFactory.getLogger(DefaultPullStrategy.class);

    private int failCnt = 0;
    private final int delayBase;
    private final int delayUpperbound;

    public DefaultPullStrategy(int delayBase, int delayUpperbound) {
        this.delayBase = delayBase;
        this.delayUpperbound = delayUpperbound;
    }

    @Override
    public long fail(boolean shouldSleep) {
        failCnt++;
        long sleepTime = (long) failCnt * delayBase;
        if (sleepTime > delayUpperbound) {
            sleepTime = delayUpperbound;
            failCnt = 0;
        }
        if (shouldSleep && sleepTime > 0) {
            if (log.isDebugEnabled()) {
                log.debug("sleep " + sleepTime + " at " + this.getClass().getSimpleName());
            }
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return sleepTime;
    }

    @Override
    public void succeess() {
        failCnt = 0;
    }

}
