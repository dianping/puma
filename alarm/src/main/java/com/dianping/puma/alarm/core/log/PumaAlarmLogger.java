package com.dianping.puma.alarm.core.log;

import com.dianping.puma.alarm.core.model.data.PullTimeDelayAlarmData;
import com.dianping.puma.alarm.core.model.data.PushTimeDelayAlarmData;
import com.dianping.puma.alarm.exception.PumaAlarmLogException;
import com.dianping.puma.common.PumaLifeCycle;

/**
 * Created by xiaotian.li on 16/3/22.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaAlarmLogger extends PumaLifeCycle {

    void logPullTimeDelay(String clientName, PullTimeDelayAlarmData data) throws PumaAlarmLogException;

    void logPushTimeDelay(String clientName, PushTimeDelayAlarmData data) throws PumaAlarmLogException;
}
