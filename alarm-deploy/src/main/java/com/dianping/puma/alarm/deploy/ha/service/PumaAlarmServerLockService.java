package com.dianping.puma.alarm.deploy.ha.service;

import com.dianping.puma.common.exception.PumaServiceException;

/**
 * Created by xiaotian.li on 16/4/5.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaAlarmServerLockService {

    void lock() throws PumaServiceException;

    void release() throws PumaServiceException;
}
