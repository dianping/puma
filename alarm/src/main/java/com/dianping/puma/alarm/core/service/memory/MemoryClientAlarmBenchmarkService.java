package com.dianping.puma.alarm.core.service.memory;

import com.dianping.puma.alarm.core.model.benchmark.PullTimeDelayAlarmBenchmark;
import com.dianping.puma.alarm.core.model.benchmark.PushTimeDelayAlarmBenchmark;
import com.dianping.puma.alarm.core.service.PumaClientAlarmBenchmarkService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapMaker;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by xiaotian.li on 16/3/20.
 * Email: lixiaotian07@gmail.com
 */
public class MemoryClientAlarmBenchmarkService implements PumaClientAlarmBenchmarkService {

    private ConcurrentMap<String, PullTimeDelayAlarmBenchmark> pullTimeDelayAlarmBenchmarkMap
            = new MapMaker().makeMap();

    private ConcurrentMap<String, PushTimeDelayAlarmBenchmark> pushTimeDelayAlarmBenchmarkMap
            = new MapMaker().makeMap();

    @Override
    public PullTimeDelayAlarmBenchmark findPullTimeDelay(String clientName) {
        return pullTimeDelayAlarmBenchmarkMap.get(clientName);
    }

    @Override
    public PushTimeDelayAlarmBenchmark findPushTimeDelay(String clientName) {
        return pushTimeDelayAlarmBenchmarkMap.get(clientName);
    }

    @Override
    public Map<String, PullTimeDelayAlarmBenchmark> findPullTimeDelayAll() {
        return ImmutableMap.copyOf(pullTimeDelayAlarmBenchmarkMap);
    }

    @Override
    public Map<String, PushTimeDelayAlarmBenchmark> findPushTimeDelayAll() {
        return ImmutableMap.copyOf(pushTimeDelayAlarmBenchmarkMap);
    }

    @Override
    public void replacePullTimeDelay(String clientName, PullTimeDelayAlarmBenchmark benchmark) {
        pullTimeDelayAlarmBenchmarkMap.put(clientName, benchmark);
    }

    @Override
    public void replacePushTimeDelay(String clientName, PushTimeDelayAlarmBenchmark benchmark) {
        pushTimeDelayAlarmBenchmarkMap.put(clientName, benchmark);
    }
}
