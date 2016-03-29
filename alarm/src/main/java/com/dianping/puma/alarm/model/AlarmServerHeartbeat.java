package com.dianping.puma.alarm.model;

import java.util.Date;

/**
 * Created by xiaotian.li on 16/3/29.
 * Email: lixiaotian07@gmail.com
 */
public class AlarmServerHeartbeat {

    private Date heartbeatTime;

    public Date getHeartbeatTime() {
        return heartbeatTime;
    }

    public void setHeartbeatTime(Date heartbeatTime) {
        this.heartbeatTime = heartbeatTime;
    }
}
