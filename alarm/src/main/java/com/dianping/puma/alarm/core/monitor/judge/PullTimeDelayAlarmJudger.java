package com.dianping.puma.alarm.core.monitor.judge;

import com.dianping.puma.alarm.core.model.benchmark.AlarmBenchmark;
import com.dianping.puma.alarm.core.model.benchmark.PullTimeDelayAlarmBenchmark;
import com.dianping.puma.alarm.core.model.data.AlarmData;
import com.dianping.puma.alarm.core.model.data.PullTimeDelayAlarmData;
import com.dianping.puma.alarm.core.model.state.AlarmState;
import com.dianping.puma.alarm.core.model.state.PullTimeDelayAlarmState;
import com.dianping.puma.alarm.exception.PumaAlarmJudgeException;
import com.dianping.puma.alarm.exception.PumaAlarmJudgeUnsupportedException;
import com.dianping.puma.common.AbstractPumaLifeCycle;

/**
 * Created by xiaotian.li on 16/3/15.
 * Email: lixiaotian07@gmail.com
 */
public class PullTimeDelayAlarmJudger extends AbstractPumaLifeCycle implements PumaAlarmJudger {

    @Override
    public AlarmState judge(AlarmData data, AlarmBenchmark benchmark) throws PumaAlarmJudgeException {
        if (!(data instanceof PullTimeDelayAlarmData)) {
            throw new PumaAlarmJudgeUnsupportedException("unsupported data[%s]", data);
        }

        if (!(benchmark instanceof PullTimeDelayAlarmBenchmark)) {
            throw new PumaAlarmJudgeUnsupportedException("unsupported benchmark[%s]", benchmark);
        }

        PullTimeDelayAlarmData pullTimeDelayAlarmData = (PullTimeDelayAlarmData) data;
        PullTimeDelayAlarmBenchmark pullTimeDelayAlarmBenchmark = (PullTimeDelayAlarmBenchmark) benchmark;

        PullTimeDelayAlarmState state = new PullTimeDelayAlarmState();

        if (!pullTimeDelayAlarmBenchmark.isPullTimeDelayAlarm()) {
            state.setAlarm(false);
        } else {
            long minPullTimeDelayInSecond = pullTimeDelayAlarmBenchmark.getMinPullTimeDelayInSecond();
            long maxPullTimeDelayInSecond = pullTimeDelayAlarmBenchmark.getMaxPullTimeDelayInSecond();

            long pullTimeDelayInSecond = pullTimeDelayAlarmData.getPullTimeDelayInSecond();

            if (pullTimeDelayInSecond >= minPullTimeDelayInSecond
                    && pullTimeDelayInSecond <= maxPullTimeDelayInSecond) {
                state.setAlarm(false);
            } else {
                state.setAlarm(true);
            }
        }

        return state;
    }
}
