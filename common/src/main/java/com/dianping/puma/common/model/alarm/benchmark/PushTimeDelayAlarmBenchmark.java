package com.dianping.puma.common.model.alarm.benchmark;

/**
 * Created by xiaotian.li on 16/3/17.
 * Email: lixiaotian07@gmail.com
 */
public class PushTimeDelayAlarmBenchmark extends AlarmBenchmark {

    private long pushTimeDelayInSecond;

    public long getPushTimeDelayInSecond() {
        return pushTimeDelayInSecond;
    }

    public void setPushTimeDelayInSecond(long pushTimeDelayInSecond) {
        this.pushTimeDelayInSecond = pushTimeDelayInSecond;
    }
}
