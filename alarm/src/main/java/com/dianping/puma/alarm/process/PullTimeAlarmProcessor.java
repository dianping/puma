package com.dianping.puma.alarm.process;

import com.dianping.puma.alarm.exception.PumaAlarmProcessException;
import com.dianping.puma.alarm.exception.PumaAlarmProcessUnsupportedException;
import com.dianping.puma.alarm.model.data.PullTimeDelayAlarmData;
import com.dianping.puma.alarm.model.data.PumaAlarmData;
import com.dianping.puma.alarm.model.raw.PullTimeDelayAlarmRawData;
import com.dianping.puma.alarm.model.raw.PumaAlarmRawData;
import com.dianping.puma.common.AbstractPumaLifeCycle;
import com.dianping.puma.common.utils.Clock;

/**
 * Created by xiaotian.li on 16/3/15.
 * Email: lixiaotian07@gmail.com
 */
public class PullTimeAlarmProcessor extends AbstractPumaLifeCycle implements PumaAlarmProcessor {

    private Clock clock;

    @Override
    public PumaAlarmData process(PumaAlarmRawData rawData) throws PumaAlarmProcessException {
        if (!(rawData instanceof PullTimeDelayAlarmRawData)) {
            throw new PumaAlarmProcessUnsupportedException("unsupported raw data[%s].", rawData);
        }

        PullTimeDelayAlarmRawData pullTimeDelayAlarmRawData
                = (PullTimeDelayAlarmRawData) rawData;
        long pullTimeDelayInSecond = clock.getTimestamp() - pullTimeDelayAlarmRawData.getPullTime();

        PullTimeDelayAlarmData pullTimeDelayAlarmData = new PullTimeDelayAlarmData();
        pullTimeDelayAlarmData.setPullTimeDelayInSecond(pullTimeDelayInSecond);

        return pullTimeDelayAlarmData;
    }
}
