package com.dianping.puma.alarm.core.monitor.filter;

import com.dianping.puma.alarm.core.model.AlarmContext;
import com.dianping.puma.alarm.core.model.AlarmResult;
import com.dianping.puma.alarm.core.model.state.AlarmState;
import com.dianping.puma.alarm.core.model.strategy.AlarmStrategy;
import com.dianping.puma.alarm.core.model.strategy.ExponentialAlarmStrategy;
import com.dianping.puma.alarm.exception.PumaAlarmFilterException;
import com.dianping.puma.alarm.exception.PumaAlarmFilterUnsupportedException;
import com.dianping.puma.common.utils.Clock;
import com.google.common.collect.MapMaker;

import java.util.concurrent.ConcurrentMap;

/**
 * Created by xiaotian.li on 16/3/17.
 * Email: lixiaotian07@gmail.com
 */
public class ExponentialAlarmFilter extends AbstractPumaAlarmFilter {

    private Clock clock = new Clock();

    private ConcurrentMap<String, Long> lastAlarmTimeMap = new MapMaker().makeMap();

    private ConcurrentMap<String, Long> nextAlarmIntervalMap = new MapMaker().makeMap();

    @Override
    public AlarmResult filter(AlarmContext context, AlarmState state, AlarmStrategy strategy)
            throws PumaAlarmFilterException {
        if (!(strategy instanceof ExponentialAlarmStrategy)) {
            throw new PumaAlarmFilterUnsupportedException("unsupported alarm strategy[%s]", strategy);
        }

        ExponentialAlarmStrategy exponentialAlarmStrategy = (ExponentialAlarmStrategy) strategy;

        AlarmResult result = new AlarmResult();
        String mnemonic = generateMnemonic(
                context.getNamespace(), context.getName(), state.getClass().getSimpleName());

        if (!state.isAlarm()) {
            lastAlarmTimeMap.remove(mnemonic);
            nextAlarmIntervalMap.remove(mnemonic);
            result.setAlarm(false);
        } else {

            if (!lastAlarmTimeMap.containsKey(mnemonic)) {
                lastAlarmTimeMap.put(mnemonic, clock.getTimestamp());
                long minExponentialAlarmIntervalInSecond
                        = exponentialAlarmStrategy.getMinExponentialAlarmIntervalInSecond();
                nextAlarmIntervalMap.put(mnemonic, minExponentialAlarmIntervalInSecond);
                result.setAlarm(true);
            } else {
                long nextAlarmInterval = nextAlarmIntervalMap.get(mnemonic);
                long lastAlarmTime = lastAlarmTimeMap.get(mnemonic);
                long now = clock.getTimestamp();
                long duration = now - lastAlarmTime;

                long maxExponentialAlarmIntervalInSecond
                        = exponentialAlarmStrategy.getMaxExponentialAlarmIntervalInSecond();

                if (duration < nextAlarmInterval) {
                    result.setAlarm(false);
                } else {
                    lastAlarmTimeMap.put(mnemonic, clock.getTimestamp());
                    nextAlarmInterval = nextAlarmInterval << 1;
                    nextAlarmInterval = (nextAlarmInterval > maxExponentialAlarmIntervalInSecond)
                            ? maxExponentialAlarmIntervalInSecond : nextAlarmInterval;
                    nextAlarmIntervalMap.put(mnemonic, nextAlarmInterval);
                    result.setAlarm(true);
                }
            }
        }

        return result;
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }
}
