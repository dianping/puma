package com.dianping.puma.alarm.control;

import com.dianping.puma.alarm.exception.PumaAlarmControlException;
import com.dianping.puma.common.PumaLifeCycle;
import com.dianping.puma.common.model.alarm.result.AlarmResult;
import com.dianping.puma.common.model.alarm.strategy.AlarmStrategy;

/**
 * Created by xiaotian.li on 16/3/16.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaAlarmController extends PumaLifeCycle {

    AlarmResult control(String clientName, AlarmResult result, AlarmStrategy strategy)
        throws PumaAlarmControlException;
}
