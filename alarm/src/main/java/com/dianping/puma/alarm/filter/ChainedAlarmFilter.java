package com.dianping.puma.alarm.filter;

import com.dianping.puma.alarm.exception.PumaAlarmFilterException;
import com.dianping.puma.alarm.exception.PumaAlarmFilterUnsupportedException;
import com.dianping.puma.alarm.model.AlarmContext;
import com.dianping.puma.alarm.model.AlarmResult;
import com.dianping.puma.alarm.model.state.AlarmState;
import com.dianping.puma.alarm.model.strategy.AlarmStrategy;

import java.util.List;

/**
 * Created by xiaotian.li on 16/3/16.
 * Email: lixiaotian07@gmail.com
 */
public class ChainedAlarmFilter extends AbstractPumaAlarmFilter {

    private List<PumaAlarmFilter> filters;

    @Override
    public void start() {
        super.start();

        for (PumaAlarmFilter controller: filters) {
            controller.start();
        }
    }

    @Override
    public void stop() {
        super.stop();

        for (PumaAlarmFilter controller: filters) {
            controller.stop();
        }
    }

    @Override
    public AlarmResult filter(AlarmContext context, AlarmState state, AlarmStrategy strategy)
            throws PumaAlarmFilterException {
        for (PumaAlarmFilter filter : filters) {
            try {
                return filter.filter(context, state, strategy);
            } catch (PumaAlarmFilterUnsupportedException ignore) {
            }
        }

        throw new PumaAlarmFilterUnsupportedException("unsupported alarm strategy[%s]", strategy);
    }

    public void setFilters(List<PumaAlarmFilter> filters) {
        this.filters = filters;
    }
}
