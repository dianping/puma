package com.dianping.puma.alarm.ha.service;

import com.dianping.puma.alarm.ha.model.AlarmServerLeader;
import com.dianping.puma.common.exception.PumaServiceException;

/**
 * Created by xiaotian.li on 16/4/5.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaAlarmServerLeaderService {

    AlarmServerLeader findLeader() throws PumaServiceException;

    boolean takeLeader(AlarmServerLeader leader) throws PumaServiceException;

    void  releaseLeader(String host) throws PumaServiceException;
}
