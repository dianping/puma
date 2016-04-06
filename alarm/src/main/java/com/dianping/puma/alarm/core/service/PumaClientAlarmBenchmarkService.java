package com.dianping.puma.alarm.core.service;

import com.dianping.puma.alarm.core.model.benchmark.PullTimeDelayAlarmBenchmark;
import com.dianping.puma.alarm.core.model.benchmark.PushTimeDelayAlarmBenchmark;

import java.util.Map;

/**
 * Created by xiaotian.li on 16/3/20.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaClientAlarmBenchmarkService {

    PullTimeDelayAlarmBenchmark findPullTimeDelay(String clientName);

    PushTimeDelayAlarmBenchmark findPushTimeDelay(String clientName);

    Map<String, PullTimeDelayAlarmBenchmark> findPullTimeDelayAll();

    Map<String, PushTimeDelayAlarmBenchmark> findPushTimeDelayAll();

    void replacePullTimeDelay(String clientName, PullTimeDelayAlarmBenchmark benchmark);

    void replacePushTimeDelay(String clientName, PushTimeDelayAlarmBenchmark benchmark);
}
