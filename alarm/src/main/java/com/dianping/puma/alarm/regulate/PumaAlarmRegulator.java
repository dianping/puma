package com.dianping.puma.alarm.regulate;

import com.dianping.puma.alarm.exception.PumaAlarmRegulateException;
import com.dianping.puma.alarm.model.AlarmResult;
import com.dianping.puma.alarm.model.AlarmState;
import com.dianping.puma.alarm.model.strategy.AlarmStrategy;
import com.dianping.puma.common.PumaLifeCycle;

/**
 * Created by xiaotian.li on 16/3/16.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaAlarmRegulator extends PumaLifeCycle {

    AlarmResult regulate(String clientName, AlarmState state, AlarmStrategy strategy)
        throws PumaAlarmRegulateException;
}
