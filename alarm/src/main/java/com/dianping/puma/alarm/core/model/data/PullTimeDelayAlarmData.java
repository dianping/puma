package com.dianping.puma.alarm.core.model.data;

import lombok.ToString;

/**
 * Created by xiaotian.li on 16/3/16.
 * Email: lixiaotian07@gmail.com
 */
@ToString(callSuper = true)
public class PullTimeDelayAlarmData extends AlarmData {

    private long pullTimeDelayInSecond;

    public long getPullTimeDelayInSecond() {
        return pullTimeDelayInSecond;
    }

    public void setPullTimeDelayInSecond(long pullTimeDelayInSecond) {
        this.pullTimeDelayInSecond = pullTimeDelayInSecond;
    }
}
