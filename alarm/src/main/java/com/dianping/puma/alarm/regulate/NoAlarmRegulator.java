package com.dianping.puma.alarm.regulate;

import com.dianping.puma.alarm.exception.PumaAlarmRegulateException;
import com.dianping.puma.alarm.exception.PumaAlarmRegulateUnsupportedException;
import com.dianping.puma.alarm.model.AlarmResult;
import com.dianping.puma.alarm.model.AlarmState;
import com.dianping.puma.alarm.model.strategy.AlarmStrategy;
import com.dianping.puma.alarm.model.strategy.NoAlarmStrategy;
import com.dianping.puma.common.AbstractPumaLifeCycle;

/**
 * Created by xiaotian.li on 16/3/18.
 * Email: lixiaotian07@gmail.com
 */
public class NoAlarmRegulator extends AbstractPumaLifeCycle implements PumaAlarmRegulator {

    @Override
    public AlarmResult regulate(String clientName, AlarmState state, AlarmStrategy strategy)
            throws PumaAlarmRegulateException {
        if (!(strategy instanceof NoAlarmStrategy)) {
            throw new PumaAlarmRegulateUnsupportedException("unsupported alarm strategy[%s]", strategy);
        }

        AlarmResult result = new AlarmResult();
        result.setAlarm(false);
        return result;
    }
}
