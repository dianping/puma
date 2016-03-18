package com.dianping.puma.alarm.service;

import com.dianping.puma.alarm.model.benchmark.AlarmBenchmark;
import com.dianping.puma.alarm.model.data.AlarmData;
import com.dianping.puma.alarm.model.meta.AlarmMeta;
import com.dianping.puma.alarm.model.strategy.AlarmStrategy;

import java.util.List;
import java.util.Map;

/**
 * Created by xiaotian.li on 16/3/18.
 * Email: lixiaotian07@gmail.com
 */
public interface ClientAlarmService {

    Map<AlarmData, AlarmBenchmark> findDataAndBenchmark(String clientName);

    AlarmStrategy findStrategy(String clientName);

    List<AlarmMeta> findMeta(String clientName);
}
