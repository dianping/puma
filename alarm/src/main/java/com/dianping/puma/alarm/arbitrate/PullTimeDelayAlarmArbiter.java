package com.dianping.puma.alarm.arbitrate;

import com.dianping.puma.alarm.exception.PumaAlarmArbitrateException;
import com.dianping.puma.alarm.exception.PumaAlarmArbitrateUnsupportedException;
import com.dianping.puma.alarm.model.benchmark.PullTimeDelayAlarmBenchmark;
import com.dianping.puma.alarm.model.benchmark.PumaAlarmBenchmark;
import com.dianping.puma.alarm.model.data.PullTimeDelayAlarmData;
import com.dianping.puma.alarm.model.data.PumaAlarmData;
import com.dianping.puma.alarm.model.result.PumaAlarmResult;
import com.dianping.puma.common.AbstractPumaLifeCycle;

/**
 * Created by xiaotian.li on 16/3/15.
 * Email: lixiaotian07@gmail.com
 */
public class PullTimeDelayAlarmArbiter extends AbstractPumaLifeCycle implements PumaAlarmArbiter {

    @Override
    public PumaAlarmResult arbitrate(PumaAlarmBenchmark benchmark, PumaAlarmData data)
            throws PumaAlarmArbitrateException {
        if (!(benchmark instanceof PullTimeDelayAlarmBenchmark)
                || !(data instanceof PullTimeDelayAlarmData)) {
            throw new PumaAlarmArbitrateUnsupportedException("unsupported benchmark[%s] or data[%s]",
                    benchmark, data);
        }

        PullTimeDelayAlarmBenchmark pullTimeDelayAlarmBenchmark = (PullTimeDelayAlarmBenchmark) benchmark;
        long minPullTimeDelayInSecond = pullTimeDelayAlarmBenchmark.getMinPullTimeDelayInSecond();
        long maxPullTimeDelayInSecond = pullTimeDelayAlarmBenchmark.getMaxPullTimeDelayInSecond();

        PullTimeDelayAlarmData pullTimeDelayAlarmData = (PullTimeDelayAlarmData) data;
        long pullTimeInSecond = pullTimeDelayAlarmData.getPullTimeDelayInSecond();

        PumaAlarmResult result = new PumaAlarmResult();
        if (pullTimeInSecond > minPullTimeDelayInSecond
                && pullTimeInSecond < maxPullTimeDelayInSecond) {
            result.setAlarm(false);
        } else {
            result.setAlarm(true);
            result.setMessage(generateAlarmMessage(pullTimeDelayAlarmBenchmark, pullTimeDelayAlarmData));
        }

        return result;
    }

    private String generateAlarmMessage(PullTimeDelayAlarmBenchmark benchmark, PullTimeDelayAlarmData data) {
        return null;
    }
}
