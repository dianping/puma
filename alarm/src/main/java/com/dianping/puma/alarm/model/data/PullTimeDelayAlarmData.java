package com.dianping.puma.alarm.model.data;

import com.dianping.puma.alarm.model.data.PumaAlarmData;

/**
 * Created by xiaotian.li on 16/3/15.
 * Email: lixiaotian07@gmail.com
 */
public class PullTimeDelayAlarmData extends PumaAlarmData {

    private long pullTimeDelayInSecond;

    public long getPullTimeDelayInSecond() {
        return pullTimeDelayInSecond;
    }

    public void setPullTimeDelayInSecond(long pullTimeDelayInSecond) {
        this.pullTimeDelayInSecond = pullTimeDelayInSecond;
    }
}
