package com.dianping.puma.alarm.core.model.strategy;

import lombok.ToString;

/**
 * Created by xiaotian.li on 16/3/17.
 * Email: lixiaotian07@gmail.com
 */
@ToString(callSuper = true)
public class ExponentialAlarmStrategy extends AlarmStrategy {

    private long minExponentialAlarmIntervalInSecond;

    private long maxExponentialAlarmIntervalInSecond;

    public long getMinExponentialAlarmIntervalInSecond() {
        return minExponentialAlarmIntervalInSecond;
    }

    public void setMinExponentialAlarmIntervalInSecond(long minExponentialAlarmIntervalInSecond) {
        this.minExponentialAlarmIntervalInSecond = minExponentialAlarmIntervalInSecond;
    }

    public long getMaxExponentialAlarmIntervalInSecond() {
        return maxExponentialAlarmIntervalInSecond;
    }

    public void setMaxExponentialAlarmIntervalInSecond(long maxExponentialAlarmIntervalInSecond) {
        this.maxExponentialAlarmIntervalInSecond = maxExponentialAlarmIntervalInSecond;
    }
}
