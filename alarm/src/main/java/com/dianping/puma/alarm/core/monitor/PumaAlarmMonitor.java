package com.dianping.puma.alarm.core.monitor;

import com.dianping.puma.alarm.core.model.benchmark.PullTimeDelayAlarmBenchmark;
import com.dianping.puma.alarm.core.model.benchmark.PushTimeDelayAlarmBenchmark;
import com.dianping.puma.alarm.core.model.data.PullTimeDelayAlarmData;
import com.dianping.puma.alarm.core.model.data.PushTimeDelayAlarmData;
import com.dianping.puma.alarm.core.model.meta.AlarmMeta;
import com.dianping.puma.alarm.core.model.strategy.AlarmStrategy;
import com.dianping.puma.alarm.exception.PumaAlarmMonitorException;
import com.dianping.puma.common.PumaLifeCycle;

import java.util.List;

/**
 * Created by xiaotian.li on 16/3/15.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaAlarmMonitor extends PumaLifeCycle {

    PullTimeDelayAlarmData monitorPullTimeDelayData(String clientName)
            throws PumaAlarmMonitorException;

    PushTimeDelayAlarmData monitorPushTimeDelayData(String clientName)
            throws PumaAlarmMonitorException;

    PullTimeDelayAlarmBenchmark monitorPullTimeDelayBenchmark(String clientName)
            throws PumaAlarmMonitorException;

    PushTimeDelayAlarmBenchmark monitorPushTimeDelayBenchmark(String clientName)
            throws PumaAlarmMonitorException;

    AlarmStrategy monitorStrategy(String clientName) throws PumaAlarmMonitorException;

    List<AlarmMeta> monitorMetas(String clientName) throws PumaAlarmMonitorException;
}
