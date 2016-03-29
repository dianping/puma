package com.dianping.puma.alarm.service;

import com.dianping.puma.alarm.model.AlarmServerHeartbeat;

/**
 * Created by xiaotian.li on 16/3/29.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaAlarmServerHeartbeatService {

    void create(String host, AlarmServerHeartbeat heartbeat);

    int update(String host, AlarmServerHeartbeat heartbeat);

    void remove(String host);
}
