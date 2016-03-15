package com.dianping.puma.alarm.model.raw;

/**
 * Created by xiaotian.li on 16/3/15.
 * Email: lixiaotian07@gmail.com
 */
public class PullTimeDelayAlarmRawData extends PumaAlarmRawData {

    private long pullTime;

    public long getPullTime() {
        return pullTime;
    }

    public void setPullTime(long pullTime) {
        this.pullTime = pullTime;
    }
}
