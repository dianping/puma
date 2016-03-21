package com.dianping.puma.alarm.arbitrate;

import com.dianping.puma.alarm.exception.PumaAlarmArbitrateException;
import com.dianping.puma.alarm.model.benchmark.AlarmBenchmark;
import com.dianping.puma.alarm.model.data.AlarmData;
import com.dianping.puma.alarm.model.result.AlarmResult;
import com.dianping.puma.common.AbstractPumaLifeCycle;

/**
 * Created by xiaotian.li on 16/3/20.
 * Email: lixiaotian07@gmail.com
 */
public class PushTimeDelayAlarmArbiter extends AbstractPumaLifeCycle implements PumaAlarmArbiter {

    @Override
    public AlarmResult arbitrate(AlarmData data, AlarmBenchmark benchmark) throws PumaAlarmArbitrateException {
        return null;
    }
}
