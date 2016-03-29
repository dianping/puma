package com.dianping.puma.alarm.service.memory;

import com.dianping.puma.alarm.model.AlarmServerHeartbeat;
import com.dianping.puma.alarm.service.PumaAlarmServerHeartbeatService;
import com.google.common.collect.MapMaker;

import java.util.concurrent.ConcurrentMap;

/**
 * Created by xiaotian.li on 16/3/29.
 * Email: lixiaotian07@gmail.com
 */
public class MemoryAlarmServerHeartbeatService implements PumaAlarmServerHeartbeatService {

    private ConcurrentMap<String, AlarmServerHeartbeat> alarmServerHeartbeatMap = new MapMaker().makeMap();

    @Override
    public void create(String host, AlarmServerHeartbeat heartbeat) {
        AlarmServerHeartbeat oriHeartbeat = alarmServerHeartbeatMap.putIfAbsent(host, heartbeat);
        if (oriHeartbeat != null) {
            throw new IllegalArgumentException("duplicated host");
        }
    }

    @Override
    public int update(String host, AlarmServerHeartbeat heartbeat) {
        if (!alarmServerHeartbeatMap.containsKey(host)) {
            return 0;
        } else {
            alarmServerHeartbeatMap.put(host, heartbeat);
            return 1;
        }
    }

    @Override
    public void remove(String host) {
        alarmServerHeartbeatMap.remove(host);
    }
}
