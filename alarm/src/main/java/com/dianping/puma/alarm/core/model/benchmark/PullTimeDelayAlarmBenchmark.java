package com.dianping.puma.alarm.core.model.benchmark;

import lombok.ToString;

/**
 * Created by xiaotian.li on 16/3/16.
 * Email: lixiaotian07@gmail.com
 */
@ToString(callSuper = true)
public class PullTimeDelayAlarmBenchmark extends AlarmBenchmark {

    private boolean pullTimeDelayAlarm;

    private long minPullTimeDelayInSecond;

    private long maxPullTimeDelayInSecond;

    public boolean isPullTimeDelayAlarm() {
        return pullTimeDelayAlarm;
    }

    public void setPullTimeDelayAlarm(boolean pullTimeDelayAlarm) {
        this.pullTimeDelayAlarm = pullTimeDelayAlarm;
    }

    public long getMinPullTimeDelayInSecond() {
        return minPullTimeDelayInSecond;
    }

    public void setMinPullTimeDelayInSecond(long minPullTimeDelayInSecond) {
        this.minPullTimeDelayInSecond = minPullTimeDelayInSecond;
    }

    public Long getMaxPullTimeDelayInSecond() {
        return maxPullTimeDelayInSecond;
    }

    public void setMaxPullTimeDelayInSecond(long maxPullTimeDelayInSecond) {
        this.maxPullTimeDelayInSecond = maxPullTimeDelayInSecond;
    }
}
