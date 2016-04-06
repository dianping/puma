package com.dianping.puma.alarm.core.monitor.render;

import com.dianping.puma.alarm.core.model.AlarmContext;
import com.dianping.puma.alarm.core.model.AlarmMessage;
import com.dianping.puma.alarm.core.model.benchmark.AlarmBenchmark;
import com.dianping.puma.alarm.core.model.data.AlarmData;
import com.dianping.puma.alarm.exception.PumaAlarmRenderException;
import com.dianping.puma.alarm.exception.PumaAlarmRenderUnsupportedException;

import java.util.List;

/**
 * Created by xiaotian.li on 16/3/25.
 * Email: lixiaotian07@gmail.com
 */
public class ChainedAlarmRenderer extends AbstractPumaAlarmRenderer {

    private List<PumaAlarmRenderer> renderers;

    @Override
    public void start() {
        super.start();

        for (PumaAlarmRenderer renderer: renderers) {
            renderer.start();
        }
    }

    @Override
    public void stop() {
        super.stop();

        for (PumaAlarmRenderer renderer: renderers) {
            renderer.stop();
        }
    }

    @Override
    public AlarmMessage render(AlarmContext context, AlarmData data, AlarmBenchmark benchmark)
            throws PumaAlarmRenderException {
        for (PumaAlarmRenderer renderer: renderers) {
            try {
                return renderer.render(context, data, benchmark);
            } catch (PumaAlarmRenderUnsupportedException ignore) {
            }
        }

        throw new PumaAlarmRenderUnsupportedException("unsupported data[%s] or benchmark[%s]",
                data, benchmark);
    }

    public void setRenderers(List<PumaAlarmRenderer> renderers) {
        this.renderers = renderers;
    }
}
