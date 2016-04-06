package com.dianping.puma.alarm.core.model.benchmark;

import lombok.ToString;

/**
 * Created by xiaotian.li on 16/3/17.
 * Email: lixiaotian07@gmail.com
 */
@ToString(callSuper = true)
public class PushTimeDelayAlarmBenchmark extends AlarmBenchmark {

    private boolean pushTimeDelayAlarm;

    private long minPushTimeDelayInSecond;

    private long maxPushTimeDelayInSecond;

    public boolean isPushTimeDelayAlarm() {
        return pushTimeDelayAlarm;
    }

    public void setPushTimeDelayAlarm(boolean pushTimeDelayAlarm) {
        this.pushTimeDelayAlarm = pushTimeDelayAlarm;
    }

    public long getMinPushTimeDelayInSecond() {
        return minPushTimeDelayInSecond;
    }

    public void setMinPushTimeDelayInSecond(long minPushTimeDelayInSecond) {
        this.minPushTimeDelayInSecond = minPushTimeDelayInSecond;
    }

    public long getMaxPushTimeDelayInSecond() {
        return maxPushTimeDelayInSecond;
    }

    public void setMaxPushTimeDelayInSecond(long maxPushTimeDelayInSecond) {
        this.maxPushTimeDelayInSecond = maxPushTimeDelayInSecond;
    }
}
