package com.dianping.puma.alarm.core.monitor.filter;

import com.dianping.puma.alarm.core.model.AlarmContext;
import com.dianping.puma.alarm.core.model.AlarmResult;
import com.dianping.puma.alarm.core.model.state.AlarmState;
import com.dianping.puma.alarm.core.model.strategy.AlarmStrategy;
import com.dianping.puma.alarm.core.model.strategy.LinearAlarmStrategy;
import com.dianping.puma.alarm.exception.PumaAlarmFilterException;
import com.dianping.puma.alarm.exception.PumaAlarmFilterUnsupportedException;
import com.dianping.puma.common.utils.Clock;
import com.google.common.collect.MapMaker;

import java.util.concurrent.ConcurrentMap;

/**
 * Created by xiaotian.li on 16/3/16.
 * Email: lixiaotian07@gmail.com
 */
public class LinearAlarmFilter extends AbstractPumaAlarmFilter {

    private Clock clock = new Clock();

    private ConcurrentMap<String, Long> lastAlarmTimeMap = new MapMaker().makeMap();

    @Override
    public AlarmResult filter(AlarmContext context, AlarmState state, AlarmStrategy strategy)
            throws PumaAlarmFilterException {
        if (!(strategy instanceof LinearAlarmStrategy)) {
            throw new PumaAlarmFilterUnsupportedException("unsupported alarm strategy[%s]", strategy);
        }

        LinearAlarmStrategy linearAlarmStrategy = (LinearAlarmStrategy) strategy;

        AlarmResult result = new AlarmResult();
        String mnemonic = generateMnemonic(context.getNamespace(), context.getName(),
                state.getClass().getSimpleName());

        if (!state.isAlarm()) {
            lastAlarmTimeMap.remove(mnemonic);
            result.setAlarm(false);
        } else {

            if (!lastAlarmTimeMap.containsKey(mnemonic)) {
                lastAlarmTimeMap.put(mnemonic, clock.getTimestamp());
                result.setAlarm(true);
            } else {
                long lastAlarmTime = lastAlarmTimeMap.get(mnemonic);
                long now = clock.getTimestamp();
                long linearAlarmIntervalInSecond = linearAlarmStrategy.getLinearAlarmIntervalInSecond();
                if (now - lastAlarmTime > linearAlarmIntervalInSecond) {
                    lastAlarmTimeMap.put(mnemonic, now);
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
