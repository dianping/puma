package com.dianping.puma.alarm.core.monitor.render;

import com.dianping.puma.alarm.core.model.AlarmContext;
import com.dianping.puma.alarm.core.model.AlarmMessage;
import com.dianping.puma.alarm.core.model.benchmark.AlarmBenchmark;
import com.dianping.puma.alarm.core.model.benchmark.PushTimeDelayAlarmBenchmark;
import com.dianping.puma.alarm.core.model.data.AlarmData;
import com.dianping.puma.alarm.core.model.data.PushTimeDelayAlarmData;
import com.dianping.puma.alarm.exception.PumaAlarmRenderException;
import com.dianping.puma.alarm.exception.PumaAlarmRenderUnsupportedException;

/**
 * Created by xiaotian.li on 16/3/25.
 * Email: lixiaotian07@gmail.com
 */
public class PushTimeDelayAlarmRenderer extends AbstractPumaAlarmRenderer {

    @Override
    public AlarmMessage render(AlarmContext context, AlarmData data, AlarmBenchmark benchmark)
            throws PumaAlarmRenderException {

        if (!(data instanceof PushTimeDelayAlarmData)) {
            throw new PumaAlarmRenderUnsupportedException("unsupported data[%s]", data);
        }

        if (!(benchmark instanceof PushTimeDelayAlarmBenchmark)) {
            throw new PumaAlarmRenderUnsupportedException("unsupported benchmark[%s]", benchmark);
        }

        AlarmMessage message = new AlarmMessage();

        PushTimeDelayAlarmData pushTimeDelayAlarmData = (PushTimeDelayAlarmData) data;
        PushTimeDelayAlarmBenchmark pushTimeDelayAlarmBenchmark = (PushTimeDelayAlarmBenchmark) benchmark;

        String title = String.format(titleTemplate, context.getName());
        String content = String.format(
                contentTemplate,
                pushTimeDelayAlarmData.getPushTimeDelayInSecond(),
                pushTimeDelayAlarmBenchmark.getMinPushTimeDelayInSecond(),
                pushTimeDelayAlarmBenchmark.getMaxPushTimeDelayInSecond());

        message.setTitle(title);
        message.setContent(content);

        return message;
    }
}
