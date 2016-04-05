package com.dianping.puma.alarm.deploy.ha;

import com.dianping.puma.alarm.deploy.exception.PumaAlarmServerHeartbeatManageException;

/**
 * Created by xiaotian.li on 16/4/5.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaAlarmServerHeartbeatManager {

    void start();

    void stop();

    void heartbeat() throws PumaAlarmServerHeartbeatManageException;
}
