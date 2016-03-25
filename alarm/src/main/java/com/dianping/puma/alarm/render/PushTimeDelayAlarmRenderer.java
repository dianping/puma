package com.dianping.puma.alarm.render;

import com.dianping.puma.alarm.exception.PumaAlarmRenderException;
import com.dianping.puma.alarm.exception.PumaAlarmRenderUnsupportedException;
import com.dianping.puma.alarm.model.AlarmMessage;
import com.dianping.puma.alarm.model.benchmark.AlarmBenchmark;
import com.dianping.puma.alarm.model.benchmark.PushTimeDelayAlarmBenchmark;
import com.dianping.puma.alarm.model.data.AlarmData;
import com.dianping.puma.alarm.model.data.PushTimeDelayAlarmData;

/**
 * Created by xiaotian.li on 16/3/25.
 * Email: lixiaotian07@gmail.com
 */
public class PushTimeDelayAlarmRenderer extends AbstractPumaAlarmRenderer {

    protected final String propertiesFilePath = "template/alarm-pushtimedelay.properties";

    @Override
    public AlarmMessage render(String clientName, AlarmData data, AlarmBenchmark benchmark)
            throws PumaAlarmRenderException {

        if (!(data instanceof PushTimeDelayAlarmData)) {
            throw new PumaAlarmRenderUnsupportedException("unsupported data[%s]", data);
        }

        if (!(benchmark instanceof PushTimeDelayAlarmBenchmark)) {
            throw new PumaAlarmRenderUnsupportedException("unsupported benchmark[%s]", benchmark);
        }

        if (template == null) {
            template = generateAlarmTemplate(propertiesFilePath);
        }

        AlarmMessage message = new AlarmMessage();

        PushTimeDelayAlarmData pushTimeDelayAlarmData = (PushTimeDelayAlarmData) data;
        PushTimeDelayAlarmBenchmark pushTimeDelayAlarmBenchmark = (PushTimeDelayAlarmBenchmark) benchmark;

        String title = String.format(template.getTitleTemplate(), clientName);
        String content = String.format(template.getContentTemplate(),
                pushTimeDelayAlarmData.getPushTimeDelayInSecond(),
                pushTimeDelayAlarmBenchmark.getMinPushTimeDelayInSecond(),
                pushTimeDelayAlarmBenchmark.getMaxPushTimeDelayInSecond());

        message.setTitle(title);
        message.setContent(content);

        return message;
    }
}
