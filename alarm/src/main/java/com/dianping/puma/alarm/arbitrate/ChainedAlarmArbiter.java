package com.dianping.puma.alarm.arbitrate;

import com.dianping.puma.alarm.exception.PumaAlarmArbitrateException;
import com.dianping.puma.alarm.exception.PumaAlarmArbitrateUnsupportedException;
import com.dianping.puma.alarm.model.AlarmState;
import com.dianping.puma.alarm.model.benchmark.AlarmBenchmark;
import com.dianping.puma.alarm.model.data.AlarmData;
import com.dianping.puma.common.AbstractPumaLifeCycle;

import java.util.List;

/**
 * Created by xiaotian.li on 16/3/15.
 * Email: lixiaotian07@gmail.com
 */
public class ChainedAlarmArbiter extends AbstractPumaLifeCycle implements PumaAlarmArbiter {

    private List<PumaAlarmArbiter> arbiters;

    @Override
    public void start() {
        super.start();

        for (PumaAlarmArbiter arbiter: arbiters) {
            arbiter.start();
        }
    }

    @Override
    public void stop() {
        super.stop();

        for (PumaAlarmArbiter arbiter: arbiters) {
            arbiter.stop();
        }
    }

    @Override
    public AlarmState arbitrate(AlarmData data, AlarmBenchmark benchmark) throws PumaAlarmArbitrateException {
        for (PumaAlarmArbiter arbiter: arbiters) {
            try {
                return arbiter.arbitrate(data, benchmark);
            } catch (PumaAlarmArbitrateUnsupportedException ignore) {
            }
        }

        throw new PumaAlarmArbitrateUnsupportedException("unsupported data[%s] or benchmark[%s].", benchmark, data);
    }

    public void setArbiters(List<PumaAlarmArbiter> arbiters) {
        this.arbiters = arbiters;
    }
}
