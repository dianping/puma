package com.dianping.puma.alarm.ha.service.memory;

import com.dianping.puma.alarm.ha.model.AlarmServerLeader;
import com.dianping.puma.alarm.ha.service.PumaAlarmServerLeaderService;
import com.dianping.puma.common.exception.PumaServiceException;

/**
 * Created by xiaotian.li on 16/4/5.
 * Email: lixiaotian07@gmail.com
 */
public class MemoryAlarmServerLeaderService implements PumaAlarmServerLeaderService {

    @Override
    public AlarmServerLeader findLeader() throws PumaServiceException {
        return null;
    }

    @Override
    public boolean takeLeader(AlarmServerLeader leader) throws PumaServiceException {
        return false;
    }

    @Override
    public void releaseLeader(String host) throws PumaServiceException {

    }
}
