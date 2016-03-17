package com.dianping.puma.biz.service;

import com.dianping.puma.alarm.service.ClientAlarmBenchmarkService;
import com.dianping.puma.biz.convert.Converter;
import com.dianping.puma.biz.dao.ClientAlarmBenchmarkDao;
import com.dianping.puma.biz.entity.ClientAlarmBenchmarkEntity;
import com.dianping.puma.alarm.model.benchmark.AlarmBenchmark;
import com.dianping.puma.alarm.model.benchmark.PullTimeDelayAlarmBenchmark;
import com.dianping.puma.alarm.model.benchmark.PushTimeDelayAlarmBenchmark;

/**
 * Created by xiaotian.li on 16/3/17.
 * Email: lixiaotian07@gmail.com
 */
public class ClientAlarmBenchmarkServiceImpl implements ClientAlarmBenchmarkService {

    private Converter converter;

    private ClientAlarmBenchmarkDao clientAlarmBenchmarkDao;

    @Override
    public AlarmBenchmark findPullTimeDelay(String clientName) {
        ClientAlarmBenchmarkEntity entity = clientAlarmBenchmarkDao.find(clientName);
        PullTimeDelayAlarmBenchmark benchmark = converter.convert(entity, PullTimeDelayAlarmBenchmark.class);
        benchmark.setPullTimeDelayAlarm(entity.isAlarmPullTimeDelay());
        return benchmark;
    }

    @Override
    public AlarmBenchmark findPushTimeDelay(String clientName) {
        ClientAlarmBenchmarkEntity entity = clientAlarmBenchmarkDao.find(clientName);
        PushTimeDelayAlarmBenchmark benchmark = converter.convert(entity, PushTimeDelayAlarmBenchmark.class);
        benchmark.setPushTimeDelayAlarm(entity.isAlarmPushTimeDelay());
        return benchmark;
    }
}
