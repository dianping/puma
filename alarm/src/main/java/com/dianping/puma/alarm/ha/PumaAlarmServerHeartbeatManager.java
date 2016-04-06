package com.dianping.puma.alarm.ha;

import com.dianping.puma.alarm.exception.PumaAlarmServerHeartbeatManageException;
import com.dianping.puma.common.PumaLifeCycle;

/**
 * Created by xiaotian.li on 16/4/5.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaAlarmServerHeartbeatManager extends PumaLifeCycle {

    void heartbeat() throws PumaAlarmServerHeartbeatManageException;
}
