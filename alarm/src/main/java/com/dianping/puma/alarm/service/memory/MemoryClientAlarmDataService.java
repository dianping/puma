package com.dianping.puma.alarm.service.memory;

import com.dianping.puma.alarm.model.data.PullTimeDelayAlarmData;
import com.dianping.puma.alarm.model.data.PushTimeDelayAlarmData;
import com.dianping.puma.alarm.service.PumaClientAlarmDataService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapMaker;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by xiaotian.li on 16/3/20.
 * Email: lixiaotian07@gmail.com
 */
public class MemoryClientAlarmDataService implements PumaClientAlarmDataService {

    private ConcurrentMap<String, PullTimeDelayAlarmData> pullTimeDelayAlarmDataMap
            = new MapMaker().makeMap();

    private ConcurrentMap<String, PushTimeDelayAlarmData> pushTimeDelayAlarmDataMap
            = new MapMaker().makeMap();

    @Override
    public PullTimeDelayAlarmData findPullTimeDelay(String clientName) {
        return pullTimeDelayAlarmDataMap.get(clientName);
    }

    @Override
    public PushTimeDelayAlarmData findPushTimeDelay(String clientName) {
        return pushTimeDelayAlarmDataMap.get(clientName);
    }

    @Override
    public Map<String, PullTimeDelayAlarmData> findPullTimeDelayAll() {
        return ImmutableMap.copyOf(pullTimeDelayAlarmDataMap);
    }

    @Override
    public Map<String, PushTimeDelayAlarmData> findPushTimeDelayAll() {
        return ImmutableMap.copyOf(pushTimeDelayAlarmDataMap);
    }

    @Override
    public void replacePullTimeDelay(String clientName, PullTimeDelayAlarmData data) {
        pullTimeDelayAlarmDataMap.put(clientName, data);
    }

    @Override
    public void replacePushTimeDelay(String clientName, PushTimeDelayAlarmData data) {
        pushTimeDelayAlarmDataMap.put(clientName, data);
    }
}
