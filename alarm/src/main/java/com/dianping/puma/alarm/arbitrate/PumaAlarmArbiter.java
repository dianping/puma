package com.dianping.puma.alarm.arbitrate;

import com.dianping.puma.alarm.exception.PumaAlarmArbitrateException;
import com.dianping.puma.alarm.model.benchmark.PumaAlarmBenchmark;
import com.dianping.puma.alarm.model.data.PumaAlarmData;
import com.dianping.puma.alarm.model.result.PumaAlarmResult;
import com.dianping.puma.common.PumaLifeCycle;

/**
 * Created by xiaotian.li on 16/3/15.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaAlarmArbiter extends PumaLifeCycle {

    PumaAlarmResult arbitrate(PumaAlarmBenchmark benchmark, PumaAlarmData data)
            throws PumaAlarmArbitrateException;
}
