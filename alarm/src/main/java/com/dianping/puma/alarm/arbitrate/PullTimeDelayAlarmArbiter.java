package com.dianping.puma.alarm.arbitrate;

import com.dianping.puma.alarm.exception.PumaAlarmArbitrateException;
import com.dianping.puma.alarm.exception.PumaAlarmArbitrateUnsupportedException;
import com.dianping.puma.common.AbstractPumaLifeCycle;
import com.dianping.puma.common.model.alarm.benchmark.AlarmBenchmark;
import com.dianping.puma.common.model.alarm.benchmark.PullTimeDelayAlarmBenchmark;
import com.dianping.puma.common.model.alarm.data.AlarmData;
import com.dianping.puma.common.model.alarm.data.PullTimeDelayAlarmData;
import com.dianping.puma.common.model.alarm.result.AlarmResult;
import com.dianping.puma.common.model.alarm.result.PullTimeDelayAlarmResult;

/**
 * Created by xiaotian.li on 16/3/15.
 * Email: lixiaotian07@gmail.com
 */
public class PullTimeDelayAlarmArbiter extends AbstractPumaLifeCycle implements PumaAlarmArbiter {

    @Override
    public AlarmResult arbitrate(AlarmData data, AlarmBenchmark benchmark) throws PumaAlarmArbitrateException {
        if (!(data instanceof PullTimeDelayAlarmData)) {
            throw new PumaAlarmArbitrateUnsupportedException("unsupported data[%s]", data);
        }

        if (!(benchmark instanceof PullTimeDelayAlarmBenchmark)) {
            throw new PumaAlarmArbitrateUnsupportedException("unsupported benchmark[%s]", benchmark);
        }

        PullTimeDelayAlarmResult result = new PullTimeDelayAlarmResult();

        if (!benchmark.isAlarm()) {
            result.setAlarm(false);
        } else {
            PullTimeDelayAlarmBenchmark pullTimeDelayAlarmBenchmark = (PullTimeDelayAlarmBenchmark) benchmark;
            long minPullTimeDelayInSecond = pullTimeDelayAlarmBenchmark.getMinPullTimeDelayInSecond();
            long maxPullTimeDelayInSecond = pullTimeDelayAlarmBenchmark.getMaxPullTimeDelayInSecond();

            PullTimeDelayAlarmData pullTimeDelayAlarmData = (PullTimeDelayAlarmData) data;
            long pullTimeDelayInSecond = pullTimeDelayAlarmData.getPullTimeDelayInSecond();

            if (pullTimeDelayInSecond >= minPullTimeDelayInSecond
                    && pullTimeDelayInSecond <= maxPullTimeDelayInSecond) {
                result.setAlarm(false);
            } else {
                result.setAlarm(true);
            }
        }

        return result;
    }
}
