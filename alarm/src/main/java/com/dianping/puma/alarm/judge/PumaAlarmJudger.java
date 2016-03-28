package com.dianping.puma.alarm.judge;

import com.dianping.puma.alarm.exception.PumaAlarmJudgeException;
import com.dianping.puma.alarm.model.benchmark.AlarmBenchmark;
import com.dianping.puma.alarm.model.data.AlarmData;
import com.dianping.puma.alarm.model.state.AlarmState;
import com.dianping.puma.common.PumaLifeCycle;

/**
 * Created by xiaotian.li on 16/3/15.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaAlarmJudger extends PumaLifeCycle {

    AlarmState judge(AlarmData data, AlarmBenchmark benchmark) throws PumaAlarmJudgeException;
}
