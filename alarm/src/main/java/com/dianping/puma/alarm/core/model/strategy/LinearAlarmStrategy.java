package com.dianping.puma.alarm.core.model.strategy;

import lombok.ToString;

/**
 * Created by xiaotian.li on 16/3/16.
 * Email: lixiaotian07@gmail.com
 */
@ToString(callSuper = true)
public class LinearAlarmStrategy extends AlarmStrategy {

    private long linearAlarmIntervalInSecond;

    public long getLinearAlarmIntervalInSecond() {
        return linearAlarmIntervalInSecond;
    }

    public void setLinearAlarmIntervalInSecond(long linearAlarmIntervalInSecond) {
        this.linearAlarmIntervalInSecond = linearAlarmIntervalInSecond;
    }
}
