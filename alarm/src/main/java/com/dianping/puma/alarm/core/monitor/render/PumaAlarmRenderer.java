package com.dianping.puma.alarm.core.monitor.render;

import com.dianping.puma.alarm.core.model.AlarmContext;
import com.dianping.puma.alarm.core.model.AlarmMessage;
import com.dianping.puma.alarm.core.model.benchmark.AlarmBenchmark;
import com.dianping.puma.alarm.core.model.data.AlarmData;
import com.dianping.puma.alarm.exception.PumaAlarmRenderException;
import com.dianping.puma.common.PumaLifeCycle;

/**
 * Created by xiaotian.li on 16/3/25.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaAlarmRenderer extends PumaLifeCycle {

    AlarmMessage render(AlarmContext context, AlarmData data, AlarmBenchmark benchmark)
            throws PumaAlarmRenderException;
}
