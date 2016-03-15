package com.dianping.puma.alarm.arbitrate;

import com.dianping.puma.alarm.exception.PumaAlarmArbitrateException;
import com.dianping.puma.alarm.exception.PumaAlarmArbitrateUnsupportedException;
import com.dianping.puma.alarm.model.benchmark.PumaAlarmBenchmark;
import com.dianping.puma.alarm.model.data.PumaAlarmData;
import com.dianping.puma.alarm.model.result.PumaAlarmResult;
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
    public PumaAlarmResult arbitrate(PumaAlarmBenchmark benchmark, PumaAlarmData data)
            throws PumaAlarmArbitrateException {

        for (PumaAlarmArbiter arbiter: arbiters) {
            try {
                return arbiter.arbitrate(benchmark, data);
            } catch (PumaAlarmArbitrateUnsupportedException ignore) {
            }
        }

        throw new PumaAlarmArbitrateUnsupportedException("unsupported benchmark[%s] or data[%s].",
                benchmark, data);
    }

    public void setArbiters(List<PumaAlarmArbiter> arbiters) {
        this.arbiters = arbiters;
    }
}
