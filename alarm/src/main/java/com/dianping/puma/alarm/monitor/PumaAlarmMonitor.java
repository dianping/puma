package com.dianping.puma.alarm.monitor;

import com.dianping.puma.alarm.exception.PumaAlarmMonitorException;
import com.dianping.puma.common.PumaLifeCycle;
import com.dianping.puma.alarm.model.benchmark.AlarmBenchmark;
import com.dianping.puma.alarm.model.data.AlarmData;
import com.dianping.puma.alarm.model.meta.AlarmMeta;
import com.dianping.puma.alarm.model.strategy.AlarmStrategy;

/**
 * Created by xiaotian.li on 16/3/15.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaAlarmMonitor extends PumaLifeCycle {

    AlarmData monitorData(String clientName) throws PumaAlarmMonitorException;

    AlarmBenchmark monitorBenchmark(String clientName) throws PumaAlarmMonitorException;

    AlarmMeta monitorMeta(String clientName) throws PumaAlarmMonitorException;

    AlarmStrategy monitorStrategy(String clientName) throws PumaAlarmMonitorException;
}
