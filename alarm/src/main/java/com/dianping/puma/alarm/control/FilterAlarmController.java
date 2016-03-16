package com.dianping.puma.alarm.control;

import com.dianping.puma.alarm.exception.PumaAlarmControlException;
import com.dianping.puma.alarm.exception.PumaAlarmControlUnsupportedException;
import com.dianping.puma.common.AbstractPumaLifeCycle;
import com.dianping.puma.common.model.alarm.result.AlarmResult;
import com.dianping.puma.common.model.alarm.strategy.AlarmStrategy;

import java.util.List;

/**
 * Created by xiaotian.li on 16/3/16.
 * Email: lixiaotian07@gmail.com
 */
public class FilterAlarmController extends AbstractPumaLifeCycle implements PumaAlarmController {

    private List<PumaAlarmController> controllers;

    @Override
    public void start() {
        super.start();

        for (PumaAlarmController controller: controllers) {
            controller.start();
        }
    }

    @Override
    public void stop() {
        super.stop();

        for (PumaAlarmController controller: controllers) {
            controller.stop();
        }
    }

    @Override
    public AlarmResult control(String clientName, AlarmResult result, AlarmStrategy strategy)
            throws PumaAlarmControlException {
        for (PumaAlarmController controller: controllers) {
            try {
                return controller.control(clientName, result, strategy);
            } catch (PumaAlarmControlUnsupportedException ignore) {
            }
        }

        throw new PumaAlarmControlUnsupportedException("unsupported alarm strategy[%s]", strategy);
    }

    public void setControllers(List<PumaAlarmController> controllers) {
        this.controllers = controllers;
    }
}
