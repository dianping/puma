package com.dianping.puma.alarm.model;

import lombok.ToString;

/**
 * Created by xiaotian.li on 16/3/25.
 * Email: lixiaotian07@gmail.com
 */
@ToString
public class AlarmState {

    private boolean alarm;

    public boolean isAlarm() {
        return alarm;
    }

    public void setAlarm(boolean alarm) {
        this.alarm = alarm;
    }
}
