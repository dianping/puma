package com.dianping.puma.alarm.core.monitor.judge;

import com.dianping.puma.alarm.core.model.benchmark.AlarmBenchmark;
import com.dianping.puma.alarm.core.model.benchmark.PushTimeDelayAlarmBenchmark;
import com.dianping.puma.alarm.core.model.data.AlarmData;
import com.dianping.puma.alarm.core.model.data.PushTimeDelayAlarmData;
import com.dianping.puma.alarm.core.model.state.AlarmState;
import com.dianping.puma.alarm.core.model.state.PushTimeDelayAlarmState;
import com.dianping.puma.alarm.exception.PumaAlarmJudgeException;
import com.dianping.puma.alarm.exception.PumaAlarmJudgeUnsupportedException;
import com.dianping.puma.common.AbstractPumaLifeCycle;

/**
 * Created by xiaotian.li on 16/3/20.
 * Email: lixiaotian07@gmail.com
 */
public class PushTimeDelayAlarmJudger extends AbstractPumaLifeCycle implements PumaAlarmJudger {

    @Override
    public AlarmState judge(AlarmData data, AlarmBenchmark benchmark) throws PumaAlarmJudgeException {
        if (!(data instanceof PushTimeDelayAlarmData)) {
            throw new PumaAlarmJudgeUnsupportedException("unsupported data[%s]", data);
        }

        if (!(benchmark instanceof PushTimeDelayAlarmBenchmark)) {
            throw new PumaAlarmJudgeUnsupportedException("unsupported benchmark[%s]", benchmark);
        }

        PushTimeDelayAlarmData pushTimeDelayAlarmData = (PushTimeDelayAlarmData) data;
        PushTimeDelayAlarmBenchmark pushTimeDelayAlarmBenchmark = (PushTimeDelayAlarmBenchmark) benchmark;

        PushTimeDelayAlarmState state = new PushTimeDelayAlarmState();

        if (!pushTimeDelayAlarmBenchmark.isPushTimeDelayAlarm()) {
            state.setAlarm(false);
        } else {
            long minPushTimeDelayInSecond = pushTimeDelayAlarmBenchmark.getMinPushTimeDelayInSecond();
            long maxPushTimeDelayInSecond = pushTimeDelayAlarmBenchmark.getMaxPushTimeDelayInSecond();

            long pushTimeDelayInSecond = pushTimeDelayAlarmData.getPushTimeDelayInSecond();

            if (pushTimeDelayInSecond >= minPushTimeDelayInSecond
                    && pushTimeDelayInSecond <= maxPushTimeDelayInSecond) {
                state.setAlarm(false);
            } else {
                state.setAlarm(true);
            }
        }

        return state;
    }
}
