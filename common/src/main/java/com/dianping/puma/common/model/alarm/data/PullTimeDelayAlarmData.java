package com.dianping.puma.common.model.alarm.data;

/**
 * Created by xiaotian.li on 16/3/16.
 * Email: lixiaotian07@gmail.com
 */
public class PullTimeDelayAlarmData extends AlarmData {

    private long pullTimeDelayInSecond;

    public long getPullTimeDelayInSecond() {
        return pullTimeDelayInSecond;
    }

    public void setPullTimeDelayInSecond(long pullTimeDelayInSecond) {
        this.pullTimeDelayInSecond = pullTimeDelayInSecond;
    }
}
