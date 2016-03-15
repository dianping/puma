package com.dianping.puma.alarm.monitor;

import com.dianping.puma.alarm.exception.PumaAlarmMonitorException;
import com.dianping.puma.alarm.model.benchmark.PumaAlarmBenchmark;
import com.dianping.puma.alarm.model.raw.PumaAlarmRawData;
import com.dianping.puma.common.PumaLifeCycle;

/**
 * Created by xiaotian.li on 16/3/15.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaAlarmMonitor extends PumaLifeCycle {

    PumaAlarmBenchmark getBenchmark(String clientName) throws PumaAlarmMonitorException;

    PumaAlarmRawData getRawData(String clientName) throws PumaAlarmMonitorException;
}
