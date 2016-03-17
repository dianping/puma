package com.dianping.puma.alarm.regulate;

import com.dianping.puma.alarm.exception.PumaAlarmRegulateException;
import com.dianping.puma.alarm.exception.PumaAlarmRegulateUnsupportedException;
import com.dianping.puma.common.AbstractPumaLifeCycle;
import com.dianping.puma.alarm.model.result.AlarmResult;
import com.dianping.puma.alarm.model.strategy.AlarmStrategy;

import java.util.List;

/**
 * Created by xiaotian.li on 16/3/16.
 * Email: lixiaotian07@gmail.com
 */
public class ChainedAlarmRegulator extends AbstractPumaLifeCycle implements PumaAlarmRegulator {

    private List<PumaAlarmRegulator> controllers;

    @Override
    public void start() {
        super.start();

        for (PumaAlarmRegulator controller: controllers) {
            controller.start();
        }
    }

    @Override
    public void stop() {
        super.stop();

        for (PumaAlarmRegulator controller: controllers) {
            controller.stop();
        }
    }

    @Override
    public AlarmResult regulate(String clientName, AlarmResult result, AlarmStrategy strategy)
            throws PumaAlarmRegulateException {
        for (PumaAlarmRegulator controller: controllers) {
            try {
                return controller.regulate(clientName, result, strategy);
            } catch (PumaAlarmRegulateUnsupportedException ignore) {
            }
        }

        throw new PumaAlarmRegulateUnsupportedException("unsupported alarm strategy[%s]", strategy);
    }

    public void setControllers(List<PumaAlarmRegulator> controllers) {
        this.controllers = controllers;
    }
}
