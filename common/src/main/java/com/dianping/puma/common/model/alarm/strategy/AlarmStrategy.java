package com.dianping.puma.common.model.alarm.strategy;

/**
 * Created by xiaotian.li on 16/3/16.
 * Email: lixiaotian07@gmail.com
 */
public class AlarmStrategy {

    private boolean linearAlarm;

    private long linearAlarmIntervalInSecond;

    public boolean isLinearAlarm() {
        return linearAlarm;
    }

    public void setLinearAlarm(boolean linearAlarm) {
        this.linearAlarm = linearAlarm;
    }

    public long getLinearAlarmIntervalInSecond() {
        return linearAlarmIntervalInSecond;
    }

    public void setLinearAlarmIntervalInSecond(long linearAlarmIntervalInSecond) {
        this.linearAlarmIntervalInSecond = linearAlarmIntervalInSecond;
    }
}
