package com.dianping.puma.biz.service.remote;

import com.dianping.puma.alarm.core.model.benchmark.PullTimeDelayAlarmBenchmark;
import com.dianping.puma.alarm.core.model.benchmark.PushTimeDelayAlarmBenchmark;
import com.dianping.puma.alarm.core.service.PumaClientAlarmBenchmarkService;
import com.dianping.puma.common.convert.Converter;
import com.dianping.puma.biz.dao.ClientAlarmBenchmarkDao;
import com.dianping.puma.biz.entity.ClientAlarmBenchmarkEntity;

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
        ClientAlarmBenchmarkEntity entity = clientAlarmBenchmarkDao.find(clientName);
        return converter.convert(entity, PullTimeDelayAlarmBenchmark.class);
    }

    @Override
    public PushTimeDelayAlarmBenchmark findPushTimeDelay(String clientName) {
        ClientAlarmBenchmarkEntity entity = clientAlarmBenchmarkDao.find(clientName);
        return converter.convert(entity, PushTimeDelayAlarmBenchmark.class);
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

    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    public void setClientAlarmBenchmarkDao(ClientAlarmBenchmarkDao clientAlarmBenchmarkDao) {
        this.clientAlarmBenchmarkDao = clientAlarmBenchmarkDao;
    }
}
