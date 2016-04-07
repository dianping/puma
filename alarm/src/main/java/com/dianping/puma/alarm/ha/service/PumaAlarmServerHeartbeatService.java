package com.dianping.puma.alarm.ha.service;

import com.dianping.puma.alarm.ha.model.AlarmServerHeartbeat;
import com.dianping.puma.common.exception.PumaServiceException;

import java.util.List;

/**
 * Created by xiaotian.li on 16/4/5.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaAlarmServerHeartbeatService {

    List<AlarmServerHeartbeat> findAll() throws PumaServiceException;

    AlarmServerHeartbeat findHeartbeat(String host) throws PumaServiceException;

    void heartbeat(AlarmServerHeartbeat heartbeat) throws PumaServiceException;
}
