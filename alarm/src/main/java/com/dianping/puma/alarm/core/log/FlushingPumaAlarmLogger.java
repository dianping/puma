package com.dianping.puma.alarm.core.log;

import com.dianping.puma.alarm.core.model.data.PullTimeDelayAlarmData;
import com.dianping.puma.alarm.core.model.data.PushTimeDelayAlarmData;
import com.dianping.puma.alarm.core.service.PumaClientAlarmDataService;
import com.dianping.puma.alarm.exception.PumaAlarmLogException;
import com.dianping.puma.common.AbstractPumaLifeCycle;

/**
 * Created by xiaotian.li on 16/3/22.
 * Email: lixiaotian07@gmail.com
 */
public class FlushingPumaAlarmLogger extends AbstractPumaLifeCycle implements PumaAlarmLogger {

    private PumaClientAlarmDataService pumaClientAlarmDataService;

    @Override
    public void logPullTimeDelay(String clientName, PullTimeDelayAlarmData data) throws PumaAlarmLogException {
        try {
            pumaClientAlarmDataService.updatePullTimeDelay(clientName, data);
        } catch (Throwable t) {
            throw new PumaAlarmLogException("Failed to log puma client[%s] pull time delay[%s].",
                    clientName, data);
        }
    }

    @Override
    public void logPushTimeDelay(String clientName, PushTimeDelayAlarmData data) throws PumaAlarmLogException {
        try {
            pumaClientAlarmDataService.updatePushTimeDelay(clientName, data);
        } catch (Throwable t) {
            throw new PumaAlarmLogException("Failed to log puma client[%s] push time delay[%s].",
                    clientName, data);
        }
    }

    public void setPumaClientAlarmDataService(PumaClientAlarmDataService pumaClientAlarmDataService) {
        this.pumaClientAlarmDataService = pumaClientAlarmDataService;
    }
}
