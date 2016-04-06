package com.dianping.puma.alarm.core.monitor.judge;

import com.dianping.puma.alarm.core.model.benchmark.AlarmBenchmark;
import com.dianping.puma.alarm.core.model.data.AlarmData;
import com.dianping.puma.alarm.core.model.state.AlarmState;
import com.dianping.puma.alarm.exception.PumaAlarmJudgeException;
import com.dianping.puma.common.PumaLifeCycle;

/**
 * Created by xiaotian.li on 16/3/15.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaAlarmJudger extends PumaLifeCycle {

    AlarmState judge(AlarmData data, AlarmBenchmark benchmark) throws PumaAlarmJudgeException;
}
