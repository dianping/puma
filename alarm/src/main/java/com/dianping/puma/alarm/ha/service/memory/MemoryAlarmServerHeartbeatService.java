package com.dianping.puma.alarm.ha.service.memory;


import com.dianping.puma.alarm.ha.model.AlarmServerHeartbeat;
import com.dianping.puma.alarm.ha.service.PumaAlarmServerHeartbeatService;
import com.dianping.puma.common.exception.PumaServiceException;

import java.util.List;

/**
 * Created by xiaotian.li on 16/4/5.
 * Email: lixiaotian07@gmail.com
 */
public class MemoryAlarmServerHeartbeatService implements PumaAlarmServerHeartbeatService {

    @Override
    public List<AlarmServerHeartbeat> findAll() throws PumaServiceException {
        return null;
    }

    @Override
    public AlarmServerHeartbeat findHeartbeat(String host) throws PumaServiceException {
        return null;
    }

    @Override
    public void heartbeat(AlarmServerHeartbeat heartbeat) throws PumaServiceException {

    }
}
