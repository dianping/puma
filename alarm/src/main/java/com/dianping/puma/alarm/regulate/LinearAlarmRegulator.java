package com.dianping.puma.alarm.regulate;

import com.dianping.puma.alarm.exception.PumaAlarmRegulateException;
import com.dianping.puma.alarm.exception.PumaAlarmRegulateUnsupportedException;
import com.dianping.puma.alarm.model.AlarmResult;
import com.dianping.puma.alarm.model.AlarmState;
import com.dianping.puma.alarm.model.strategy.AlarmStrategy;
import com.dianping.puma.alarm.model.strategy.LinearAlarmStrategy;
import com.dianping.puma.common.AbstractPumaLifeCycle;
import com.dianping.puma.common.utils.Clock;
import com.google.common.collect.MapMaker;

import java.util.concurrent.ConcurrentMap;

/**
 * Created by xiaotian.li on 16/3/16.
 * Email: lixiaotian07@gmail.com
 */
public class LinearAlarmRegulator extends AbstractPumaLifeCycle implements PumaAlarmRegulator {

    private Clock clock = new Clock();

    private ConcurrentMap<String, Long> lastAlarmTimeMap = new MapMaker().makeMap();

    @Override
    public AlarmResult regulate(String clientName, AlarmState state, AlarmStrategy strategy)
            throws PumaAlarmRegulateException {
        if (!(strategy instanceof LinearAlarmStrategy)) {
            throw new PumaAlarmRegulateUnsupportedException("unsupported alarm strategy[%s]", strategy);
        }

        LinearAlarmStrategy linearAlarmStrategy = (LinearAlarmStrategy) strategy;

        AlarmResult result = new AlarmResult();

        if (!state.isAlarm()) {
            lastAlarmTimeMap.remove(clientName);
            result.setAlarm(false);
        } else {

            if (!lastAlarmTimeMap.containsKey(clientName)) {
                lastAlarmTimeMap.put(clientName, clock.getTimestamp());
                result.setAlarm(true);
            } else {
                long lastAlarmTime = lastAlarmTimeMap.get(clientName);
                long now = clock.getTimestamp();
                long linearAlarmIntervalInSecond = linearAlarmStrategy.getLinearAlarmIntervalInSecond();
                if (now - lastAlarmTime > linearAlarmIntervalInSecond) {
                    lastAlarmTimeMap.put(clientName, now);
                    result.setAlarm(true);
                } else {
                    result.setAlarm(false);
                }
            }
        }

        return result;
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }
}
