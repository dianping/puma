package com.dianping.puma.alarm.service;

import com.dianping.puma.common.model.ClientAlarmData;
import com.dianping.puma.common.model.alarm.data.AlarmData;
import com.dianping.puma.common.model.alarm.data.PullTimeDelayAlarmData;
import com.dianping.puma.common.model.alarm.data.PushTimeDelayAlarmData;

/**
 * Created by xiaotian.li on 16/3/8.
 * Email: lixiaotian07@gmail.com
 */
public interface ClientAlarmDataService {

    AlarmData findPullTimeDelay(String clientName);

    void createPullTimeDelay(String clientName, PullTimeDelayAlarmData data);

    void createPushTimeDelay(String clientName, PushTimeDelayAlarmData data);

    int updatePullTimeDelay(String clientName, PullTimeDelayAlarmData data);

    int updatePushTimeDelay(String clientName, PushTimeDelayAlarmData data);

    void replacePullTimeDelay(String clientName, PullTimeDelayAlarmData data);

    void replacePushTimeDelay(String clientName, PushTimeDelayAlarmData data);

    void create(String clientName, ClientAlarmData clientAlarmData);

    int updatePullTime(String clientName, ClientAlarmData clientAlarmData);

    int updatePushTime(String clientName, ClientAlarmData clientAlarmData);

    void replacePullTime(String clientName, ClientAlarmData clientAlarmData);

    void replacePushTime(String clientName, ClientAlarmData clientAlarmData);
}
