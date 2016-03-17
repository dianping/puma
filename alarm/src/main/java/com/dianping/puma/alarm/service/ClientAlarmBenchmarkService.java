package com.dianping.puma.alarm.service;

import com.dianping.puma.alarm.model.benchmark.AlarmBenchmark;

/**
 * Created by xiaotian.li on 16/3/15.
 * Email: lixiaotian07@gmail.com
 */
public interface ClientAlarmBenchmarkService {

    AlarmBenchmark findPullTimeDelay(String clientName);

    AlarmBenchmark findPushTimeDelay(String clientName);
}
