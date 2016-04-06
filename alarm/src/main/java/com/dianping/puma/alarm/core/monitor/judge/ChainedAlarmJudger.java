package com.dianping.puma.alarm.core.monitor.judge;

import com.dianping.puma.alarm.core.model.benchmark.AlarmBenchmark;
import com.dianping.puma.alarm.core.model.data.AlarmData;
import com.dianping.puma.alarm.core.model.state.AlarmState;
import com.dianping.puma.alarm.exception.PumaAlarmJudgeException;
import com.dianping.puma.alarm.exception.PumaAlarmJudgeUnsupportedException;
import com.dianping.puma.common.AbstractPumaLifeCycle;

import java.util.List;

/**
 * Created by xiaotian.li on 16/3/15.
 * Email: lixiaotian07@gmail.com
 */
public class ChainedAlarmJudger extends AbstractPumaLifeCycle implements PumaAlarmJudger {

    private List<PumaAlarmJudger> judgers;

    @Override
    public void start() {
        super.start();

        for (PumaAlarmJudger arbiter: judgers) {
            arbiter.start();
        }
    }

    @Override
    public void stop() {
        super.stop();

        for (PumaAlarmJudger arbiter: judgers) {
            arbiter.stop();
        }
    }

    @Override
    public AlarmState judge(AlarmData data, AlarmBenchmark benchmark) throws PumaAlarmJudgeException {
        for (PumaAlarmJudger judger : judgers) {
            try {
                return judger.judge(data, benchmark);
            } catch (PumaAlarmJudgeUnsupportedException ignore) {
            }
        }

        throw new PumaAlarmJudgeUnsupportedException("unsupported data[%s] or benchmark[%s].", benchmark, data);
    }

    public void setJudgers(List<PumaAlarmJudger> judgers) {
        this.judgers = judgers;
    }
}
