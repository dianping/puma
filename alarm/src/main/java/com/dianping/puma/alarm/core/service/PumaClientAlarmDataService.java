package com.dianping.puma.alarm.core.service;

import com.dianping.puma.alarm.core.model.data.PullTimeDelayAlarmData;
import com.dianping.puma.alarm.core.model.data.PushTimeDelayAlarmData;

import java.util.Map;

/**
 * Created by xiaotian.li on 16/3/20.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaClientAlarmDataService {

    PullTimeDelayAlarmData findPullTimeDelay(String clientName);

    PushTimeDelayAlarmData findPushTimeDelay(String clientName);

    Map<String, PullTimeDelayAlarmData> findPullTimeDelayAll();

    Map<String, PushTimeDelayAlarmData> findPushTimeDelayAll();

    int updatePullTimeDelay(String clientName, PullTimeDelayAlarmData data);

    int updatePushTimeDelay(String clientName, PushTimeDelayAlarmData data);

    void replacePullTimeDelay(String clientName, PullTimeDelayAlarmData data);

    void replacePushTimeDelay(String clientName, PushTimeDelayAlarmData data);
}
