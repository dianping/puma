package com.dianping.puma.alarm.control;

import com.dianping.puma.alarm.exception.PumaAlarmControlException;
import com.dianping.puma.alarm.exception.PumaAlarmControlUnsupportedException;
import com.dianping.puma.common.AbstractPumaLifeCycle;
import com.dianping.puma.common.model.alarm.result.AlarmResult;
import com.dianping.puma.common.model.alarm.strategy.AlarmStrategy;
import com.dianping.puma.common.utils.Clock;
import com.google.common.collect.MapMaker;

import java.util.concurrent.ConcurrentMap;

/**
 * Created by xiaotian.li on 16/3/16.
 * Email: lixiaotian07@gmail.com
 */
public class LinearAlarmController extends AbstractPumaLifeCycle implements PumaAlarmController {

    private Clock clock;

    private ConcurrentMap<String, Long> lastAlarmTimeMap = new MapMaker().makeMap();

    @Override
    public AlarmResult control(String clientName, AlarmResult result, AlarmStrategy strategy)
            throws PumaAlarmControlException {
        if (!strategy.isLinearAlarm()) {
            throw new PumaAlarmControlUnsupportedException("unsupported alarm strategy[%s]", strategy);
        }

        if (!result.isAlarm()) {
            lastAlarmTimeMap.remove(clientName);
            return result;
        } else {

            if (!lastAlarmTimeMap.containsKey(clientName)) {
                lastAlarmTimeMap.put(clientName, clock.getTimestamp());
                return result;
            } else {
                long lastAlarmTime = lastAlarmTimeMap.get(clientName);
                long now = clock.getTimestamp();
                long linearAlarmIntervalInSecond = strategy.getLinearAlarmIntervalInSecond();
                if (now - lastAlarmTime > linearAlarmIntervalInSecond) {
                    lastAlarmTimeMap.put(clientName, now);
                    return result;
                } else {
                    result.setAlarm(false);
                    return result;
                }
            }

        }
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }
}
