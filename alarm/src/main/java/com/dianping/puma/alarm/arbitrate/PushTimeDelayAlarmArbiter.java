package com.dianping.puma.alarm.arbitrate;

import com.dianping.puma.alarm.exception.PumaAlarmArbitrateException;
import com.dianping.puma.alarm.exception.PumaAlarmArbitrateUnsupportedException;
import com.dianping.puma.alarm.model.benchmark.AlarmBenchmark;
import com.dianping.puma.alarm.model.benchmark.PushTimeDelayAlarmBenchmark;
import com.dianping.puma.alarm.model.data.AlarmData;
import com.dianping.puma.alarm.model.data.PushTimeDelayAlarmData;
import com.dianping.puma.alarm.model.result.AlarmResult;
import com.dianping.puma.common.AbstractPumaLifeCycle;

/**
 * Created by xiaotian.li on 16/3/20.
 * Email: lixiaotian07@gmail.com
 */
public class PushTimeDelayAlarmArbiter extends AbstractPumaLifeCycle implements PumaAlarmArbiter {

    @Override
    public AlarmResult arbitrate(AlarmData data, AlarmBenchmark benchmark) throws PumaAlarmArbitrateException {
        if (!(data instanceof PushTimeDelayAlarmData)) {
            throw new PumaAlarmArbitrateUnsupportedException("unsupported data[%s]", data);
        }

        if (!(benchmark instanceof PushTimeDelayAlarmBenchmark)) {
            throw new PumaAlarmArbitrateUnsupportedException("unsupported benchmark[%s]", benchmark);
        }

        PushTimeDelayAlarmData pushTimeDelayAlarmData = (PushTimeDelayAlarmData) data;
        PushTimeDelayAlarmBenchmark pushTimeDelayAlarmBenchmark = (PushTimeDelayAlarmBenchmark) benchmark;

        AlarmResult result = new AlarmResult();

        if (!pushTimeDelayAlarmBenchmark.isPushTimeDelayAlarm()) {
            result.setAlarm(false);
        } else {
            long minPushTimeDelayInSecond = pushTimeDelayAlarmBenchmark.getMinPushTimeDelayInSecond();
            long maxPushTimeDelayInSecond = pushTimeDelayAlarmBenchmark.getMaxPushTimeDelayInSecond();

            long pushTimeDelayInSecond = pushTimeDelayAlarmData.getPushTimeDelayInSecond();

            if (pushTimeDelayInSecond >= minPushTimeDelayInSecond
                    && pushTimeDelayInSecond <= maxPushTimeDelayInSecond) {
                result.setAlarm(false);
            } else {
                result.setAlarm(true);
            }
        }

        return result;
    }
}
