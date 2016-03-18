package com.dianping.puma.alarm.regulate;

import com.dianping.puma.alarm.exception.PumaAlarmRegulateException;
import com.dianping.puma.alarm.model.result.AlarmResult;
import com.dianping.puma.alarm.model.strategy.AlarmStrategy;
import com.dianping.puma.common.AbstractPumaLifeCycle;

/**
 * Created by xiaotian.li on 16/3/18.
 * Email: lixiaotian07@gmail.com
 */
public class NoAlarmRegulator extends AbstractPumaLifeCycle implements PumaAlarmRegulator {

    @Override
    public AlarmResult regulate(String clientName, AlarmResult result, AlarmStrategy strategy)
            throws PumaAlarmRegulateException {
        return null;
    }
}
