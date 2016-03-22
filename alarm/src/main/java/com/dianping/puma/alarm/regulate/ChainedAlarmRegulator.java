package com.dianping.puma.alarm.regulate;

import com.dianping.puma.alarm.exception.PumaAlarmRegulateException;
import com.dianping.puma.alarm.exception.PumaAlarmRegulateUnsupportedException;
import com.dianping.puma.alarm.model.result.AlarmResult;
import com.dianping.puma.alarm.model.strategy.AlarmStrategy;
import com.dianping.puma.common.AbstractPumaLifeCycle;

import java.util.List;

/**
 * Created by xiaotian.li on 16/3/16.
 * Email: lixiaotian07@gmail.com
 */
public class ChainedAlarmRegulator extends AbstractPumaLifeCycle implements PumaAlarmRegulator {

    private List<PumaAlarmRegulator> regulators;

    @Override
    public void start() {
        super.start();

        for (PumaAlarmRegulator controller: regulators) {
            controller.start();
        }
    }

    @Override
    public void stop() {
        super.stop();

        for (PumaAlarmRegulator controller: regulators) {
            controller.stop();
        }
    }

    @Override
    public AlarmResult regulate(String clientName, AlarmResult result, AlarmStrategy strategy)
            throws PumaAlarmRegulateException {
        for (PumaAlarmRegulator regulator: regulators) {
            try {
                return regulator.regulate(clientName, result, strategy);
            } catch (PumaAlarmRegulateUnsupportedException ignore) {
            }
        }

        throw new PumaAlarmRegulateUnsupportedException("unsupported alarm strategy[%s]", strategy);
    }

    public void setRegulators(List<PumaAlarmRegulator> regulators) {
        this.regulators = regulators;
    }
}
