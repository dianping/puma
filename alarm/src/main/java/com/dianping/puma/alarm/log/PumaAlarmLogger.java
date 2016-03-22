package com.dianping.puma.alarm.log;

import com.dianping.puma.alarm.exception.PumaAlarmLogException;
import com.dianping.puma.common.PumaLifeCycle;

/**
 * Created by xiaotian.li on 16/3/22.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaAlarmLogger extends PumaLifeCycle {

    void logPullTime(String clientName, long pullTime) throws PumaAlarmLogException;

    void logPushTime(String clientName, long pushTime) throws PumaAlarmLogException;
}
