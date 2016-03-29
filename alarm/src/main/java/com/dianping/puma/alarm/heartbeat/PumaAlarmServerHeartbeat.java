package com.dianping.puma.alarm.heartbeat;

import com.dianping.puma.alarm.exception.PumaAlarmServerHeartbeatException;
import com.dianping.puma.alarm.model.AlarmServerHeartbeat;
import com.dianping.puma.common.PumaLifeCycle;

/**
 * Created by xiaotian.li on 16/3/29.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaAlarmServerHeartbeat extends PumaLifeCycle {

    void heartbeat(AlarmServerHeartbeat heartbeat) throws PumaAlarmServerHeartbeatException;
}
