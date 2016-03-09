package com.dianping.puma.alarm.service;

import com.dianping.puma.common.PumaLifeCycle;
import com.dianping.puma.alarm.exception.PumaAlarmServiceException;

/**
 * Created by xiaotian.li on 16/3/8.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaAlarmService extends PumaLifeCycle {

    void alarm(String destination, String title, String content) throws PumaAlarmServiceException;
}
