package com.dianping.puma.alarm.core.monitor.render;

import com.dianping.puma.alarm.core.model.AlarmContext;
import com.dianping.puma.alarm.core.model.AlarmMessage;
import com.dianping.puma.alarm.core.model.benchmark.AlarmBenchmark;
import com.dianping.puma.alarm.core.model.benchmark.PullTimeDelayAlarmBenchmark;
import com.dianping.puma.alarm.core.model.data.AlarmData;
import com.dianping.puma.alarm.core.model.data.PullTimeDelayAlarmData;
import com.dianping.puma.alarm.exception.PumaAlarmRenderException;
import com.dianping.puma.alarm.exception.PumaAlarmRenderUnsupportedException;

/**
 * Created by xiaotian.li on 16/3/25.
 * Email: lixiaotian07@gmail.com
 */
public class PullTimeDelayAlarmRenderer extends AbstractPumaAlarmRenderer {

    @Override
    public AlarmMessage render(AlarmContext context, AlarmData data, AlarmBenchmark benchmark)
            throws PumaAlarmRenderException {

        if (!(data instanceof PullTimeDelayAlarmData)) {
            throw new PumaAlarmRenderUnsupportedException("unsupported data[%s]", data);
        }

        if (!(benchmark instanceof PullTimeDelayAlarmBenchmark)) {
            throw new PumaAlarmRenderUnsupportedException("unsupported benchmark[%s]", data);
        }

        AlarmMessage message = new AlarmMessage();

        PullTimeDelayAlarmData pullTimeDelayAlarmData = (PullTimeDelayAlarmData) data;
        PullTimeDelayAlarmBenchmark pullTimeDelayAlarmBenchmark = (PullTimeDelayAlarmBenchmark) benchmark;

        String title = String.format(titleTemplate, context.getName());
        String content = String.format(
                contentTemplate,
                pullTimeDelayAlarmData.getPullTimeDelayInSecond(),
                pullTimeDelayAlarmBenchmark.getMinPullTimeDelayInSecond(),
                pullTimeDelayAlarmBenchmark.getMaxPullTimeDelayInSecond());
        message.setTitle(title);
        message.setContent(content);

        return message;
    }
}
