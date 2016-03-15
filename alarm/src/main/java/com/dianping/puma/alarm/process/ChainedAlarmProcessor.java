package com.dianping.puma.alarm.process;

import com.dianping.puma.alarm.exception.PumaAlarmProcessException;
import com.dianping.puma.alarm.exception.PumaAlarmProcessUnsupportedException;
import com.dianping.puma.alarm.model.data.PumaAlarmData;
import com.dianping.puma.alarm.model.raw.PumaAlarmRawData;
import com.dianping.puma.common.AbstractPumaLifeCycle;

import java.util.List;

/**
 * Created by xiaotian.li on 16/3/15.
 * Email: lixiaotian07@gmail.com
 */
public class ChainedAlarmProcessor extends AbstractPumaLifeCycle implements PumaAlarmProcessor {

    private List<PumaAlarmProcessor> processors;

    @Override
    public void start() {
        super.start();

        for (PumaAlarmProcessor processor: processors) {
            processor.start();
        }
    }

    @Override
    public void stop() {
        super.stop();

        for (PumaAlarmProcessor processor: processors) {
            processor.stop();
        }
    }

    @Override
    public PumaAlarmData process(PumaAlarmRawData rawData) throws PumaAlarmProcessException {
        for (PumaAlarmProcessor processor: processors) {
            try {
                processor.process(rawData);
            } catch (PumaAlarmProcessUnsupportedException ignore) {
            }
        }

        throw new PumaAlarmProcessUnsupportedException("unsupported raw data[%s]", rawData);
    }

    public void setProcessors(List<PumaAlarmProcessor> processors) {
        this.processors = processors;
    }
}
