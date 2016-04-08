package com.dianping.puma.alarm.core.service;

import com.dianping.puma.alarm.core.model.AlarmResult;
import com.dianping.puma.common.exception.PumaServiceException;

/**
 * Created by xiaotian.li on 16/4/8.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaClientAlarmResultService {

    AlarmResult find(String clientName) throws PumaServiceException;

    void create(String clientName, AlarmResult alarmResult) throws PumaServiceException;

    int update(String clientName, AlarmResult alarmResult) throws PumaServiceException;

    void delete(String clientName) throws PumaServiceException;
}
