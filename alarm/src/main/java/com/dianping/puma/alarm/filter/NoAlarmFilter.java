package com.dianping.puma.alarm.filter;

import com.dianping.puma.alarm.exception.PumaAlarmFilterException;
import com.dianping.puma.alarm.exception.PumaAlarmFilterUnsupportedException;
import com.dianping.puma.alarm.model.AlarmContext;
import com.dianping.puma.alarm.model.AlarmResult;
import com.dianping.puma.alarm.model.state.AlarmState;
import com.dianping.puma.alarm.model.strategy.AlarmStrategy;
import com.dianping.puma.alarm.model.strategy.NoAlarmStrategy;

/**
 * Created by xiaotian.li on 16/3/18.
 * Email: lixiaotian07@gmail.com
 */
public class NoAlarmFilter extends AbstractPumaAlarmFilter {

    @Override
    public AlarmResult filter(AlarmContext context, AlarmState state, AlarmStrategy strategy)
            throws PumaAlarmFilterException {
        if (!(strategy instanceof NoAlarmStrategy)) {
            throw new PumaAlarmFilterUnsupportedException("unsupported alarm strategy[%s]", strategy);
        }

        AlarmResult result = new AlarmResult();
        result.setAlarm(false);
        return result;
    }
}
