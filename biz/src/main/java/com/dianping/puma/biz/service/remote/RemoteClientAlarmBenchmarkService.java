package com.dianping.puma.biz.service.remote;

import com.dianping.puma.alarm.model.benchmark.PullTimeDelayAlarmBenchmark;
import com.dianping.puma.alarm.model.benchmark.PushTimeDelayAlarmBenchmark;
import com.dianping.puma.alarm.service.PumaClientAlarmBenchmarkService;
import com.dianping.puma.biz.convert.Converter;
import com.dianping.puma.biz.dao.ClientAlarmBenchmarkDao;

import java.util.Map;

/**
 * Created by xiaotian.li on 16/3/22.
 * Email: lixiaotian07@gmail.com
 */
public class RemoteClientAlarmBenchmarkService implements PumaClientAlarmBenchmarkService {

    private Converter converter;

    private ClientAlarmBenchmarkDao clientAlarmBenchmarkDao;

    @Override
    public PullTimeDelayAlarmBenchmark findPullTimeDelay(String clientName) {
        return null;
    }

    @Override
    public PushTimeDelayAlarmBenchmark findPushTimeDelay(String clientName) {
        return null;
    }

    @Override
    public Map<String, PullTimeDelayAlarmBenchmark> findPullTimeDelayAll() {
        return null;
    }

    @Override
    public Map<String, PushTimeDelayAlarmBenchmark> findPushTimeDelayAll() {
        return null;
    }

    @Override
    public void replacePullTimeDelay(String clientName, PullTimeDelayAlarmBenchmark benchmark) {

    }

    @Override
    public void replacePushTimeDelay(String clientName, PushTimeDelayAlarmBenchmark benchmark) {

    }
}
