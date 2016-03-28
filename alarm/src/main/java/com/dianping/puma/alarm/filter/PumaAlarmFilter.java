package com.dianping.puma.alarm.filter;

import com.dianping.puma.alarm.exception.PumaAlarmFilterException;
import com.dianping.puma.alarm.model.AlarmContext;
import com.dianping.puma.alarm.model.AlarmResult;
import com.dianping.puma.alarm.model.state.AlarmState;
import com.dianping.puma.alarm.model.strategy.AlarmStrategy;
import com.dianping.puma.common.PumaLifeCycle;

/**
 * Created by xiaotian.li on 16/3/16.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaAlarmFilter extends PumaLifeCycle {

    AlarmResult filter(AlarmContext context, AlarmState state, AlarmStrategy strategy)
        throws PumaAlarmFilterException;
}
