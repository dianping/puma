package com.dianping.puma.alarm.regulate;

import com.dianping.puma.alarm.exception.PumaAlarmRegulateException;
import com.dianping.puma.alarm.exception.PumaAlarmRegulateUnsupportedException;
import com.dianping.puma.alarm.model.AlarmResult;
import com.dianping.puma.alarm.model.AlarmState;
import com.dianping.puma.alarm.model.strategy.AlarmStrategy;
import com.dianping.puma.alarm.model.strategy.ExponentialAlarmStrategy;
import com.dianping.puma.common.AbstractPumaLifeCycle;
import com.dianping.puma.common.utils.Clock;
import com.google.common.collect.MapMaker;

import java.util.concurrent.ConcurrentMap;

/**
 * Created by xiaotian.li on 16/3/17.
 * Email: lixiaotian07@gmail.com
 */
public class ExponentialAlarmRegulator extends AbstractPumaLifeCycle implements PumaAlarmRegulator {

    private Clock clock = new Clock();

    private ConcurrentMap<String, Long> lastAlarmTimeMap = new MapMaker().makeMap();

    private ConcurrentMap<String, Long> nextAlarmIntervalMap = new MapMaker().makeMap();

    @Override
    public AlarmResult regulate(String clientName, AlarmState state, AlarmStrategy strategy)
            throws PumaAlarmRegulateException {
        if (!(strategy instanceof ExponentialAlarmStrategy)) {
            throw new PumaAlarmRegulateUnsupportedException("unsupported alarm strategy[%s]", strategy);
        }

        ExponentialAlarmStrategy exponentialAlarmStrategy = (ExponentialAlarmStrategy) strategy;

        AlarmResult result = new AlarmResult();

        if (!state.isAlarm()) {
            lastAlarmTimeMap.remove(clientName);
            nextAlarmIntervalMap.remove(clientName);
            result.setAlarm(false);
        } else {

            if (!lastAlarmTimeMap.containsKey(clientName)) {
                lastAlarmTimeMap.put(clientName, clock.getTimestamp());
                long minExponentialAlarmIntervalInSecond
                        = exponentialAlarmStrategy.getMinExponentialAlarmIntervalInSecond();
                nextAlarmIntervalMap.put(clientName, minExponentialAlarmIntervalInSecond);
                result.setAlarm(true);
            } else {
                long nextAlarmInterval = nextAlarmIntervalMap.get(clientName);
                long lastAlarmTime = lastAlarmTimeMap.get(clientName);
                long now = clock.getTimestamp();
                long duration = now - lastAlarmTime;

                long maxExponentialAlarmIntervalInSecond
                        = exponentialAlarmStrategy.getMaxExponentialAlarmIntervalInSecond();

                if (duration < nextAlarmInterval) {
                    result.setAlarm(false);
                } else {
                    lastAlarmTimeMap.put(clientName, clock.getTimestamp());
                    nextAlarmInterval = nextAlarmInterval << 1;
                    nextAlarmInterval = (nextAlarmInterval > maxExponentialAlarmIntervalInSecond)
                            ? maxExponentialAlarmIntervalInSecond : nextAlarmInterval;
                    nextAlarmIntervalMap.put(clientName, nextAlarmInterval);
                    result.setAlarm(true);
                }

                return result;
            }
        }

        return result;
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }
}
