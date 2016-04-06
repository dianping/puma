package com.dianping.puma.alarm.core.model.data;

import lombok.ToString;

/**
 * Created by xiaotian.li on 16/3/17.
 * Email: lixiaotian07@gmail.com
 */
@ToString(callSuper = true)
public class PushTimeDelayAlarmData extends AlarmData {

    private long pushTimeDelayInSecond;

    public long getPushTimeDelayInSecond() {
        return pushTimeDelayInSecond;
    }

    public void setPushTimeDelayInSecond(long pushTimeDelayInSecond) {
        this.pushTimeDelayInSecond = pushTimeDelayInSecond;
    }
}
