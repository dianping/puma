package com.dianping.puma.alarm.deploy.ha.service.memory;

import com.dianping.puma.alarm.deploy.ha.model.AlarmServerLeader;
import com.dianping.puma.alarm.deploy.ha.service.PumaAlarmServerLeaderService;
import com.dianping.puma.common.exception.PumaServiceException;

/**
 * Created by xiaotian.li on 16/4/5.
 * Email: lixiaotian07@gmail.com
 */
public class MemoryAlarmServerLeaderService implements PumaAlarmServerLeaderService {

    @Override
    public void takeLeader(AlarmServerLeader leader) throws PumaServiceException {

    }

    @Override
    public void releaseLeader(AlarmServerLeader leader) throws PumaServiceException {

    }
}
