package com.dianping.puma.common.model.alarm.data;

/**
 * Created by xiaotian.li on 16/3/17.
 * Email: lixiaotian07@gmail.com
 */
public class PushTimeDelayAlarmData extends AlarmData {

    private long pushTimeDelayInSecond;

    public long getPushTimeDelayInSecond() {
        return pushTimeDelayInSecond;
    }

    public void setPushTimeDelayInSecond(long pushTimeDelayInSecond) {
        this.pushTimeDelayInSecond = pushTimeDelayInSecond;
    }
}
