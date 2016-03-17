package com.dianping.puma.biz.service;

import com.dianping.puma.alarm.service.ClientAlarmBenchmarkService;
import com.dianping.puma.biz.convert.Converter;
import com.dianping.puma.biz.dao.ClientAlarmBenchmarkDao;
import com.dianping.puma.biz.entity.ClientAlarmBenchmarkEntity;
import com.dianping.puma.common.model.alarm.benchmark.AlarmBenchmark;
import com.dianping.puma.common.model.alarm.benchmark.PullTimeDelayAlarmBenchmark;
import com.dianping.puma.common.model.alarm.benchmark.PushTimeDelayAlarmBenchmark;

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
        benchmark.setAlarm(entity.isAlarmPullTimeDelay());
        return benchmark;
    }

    @Override
    public AlarmBenchmark findPushTimeDelay(String clientName) {
        ClientAlarmBenchmarkEntity entity = clientAlarmBenchmarkDao.find(clientName);
        PushTimeDelayAlarmBenchmark benchmark = converter.convert(entity, PushTimeDelayAlarmBenchmark.class);
        benchmark.setAlarm(entity.isAlarmPushTimeDelay());
        return benchmark;
    }
}
