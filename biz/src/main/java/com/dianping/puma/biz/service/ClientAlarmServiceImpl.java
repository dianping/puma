package com.dianping.puma.biz.service;

import com.dianping.puma.alarm.model.benchmark.AlarmBenchmark;
import com.dianping.puma.alarm.model.benchmark.PullTimeDelayAlarmBenchmark;
import com.dianping.puma.alarm.model.benchmark.PushTimeDelayAlarmBenchmark;
import com.dianping.puma.alarm.model.data.AlarmData;
import com.dianping.puma.alarm.model.data.PullTimeDelayAlarmData;
import com.dianping.puma.alarm.model.data.PushTimeDelayAlarmData;
import com.dianping.puma.alarm.model.meta.AlarmMeta;
import com.dianping.puma.alarm.model.meta.EmailAlarmMeta;
import com.dianping.puma.alarm.model.meta.SmsAlarmMeta;
import com.dianping.puma.alarm.model.meta.WeChatAlarmMeta;
import com.dianping.puma.alarm.model.strategy.AlarmStrategy;
import com.dianping.puma.alarm.model.strategy.ExponentialAlarmStrategy;
import com.dianping.puma.alarm.model.strategy.LinearAlarmStrategy;
import com.dianping.puma.alarm.model.strategy.NoAlarmStrategy;
import com.dianping.puma.alarm.service.ClientAlarmService;
import com.dianping.puma.biz.convert.Converter;
import com.dianping.puma.biz.dao.ClientAlarmBenchmarkDao;
import com.dianping.puma.biz.dao.ClientAlarmDataDao;
import com.dianping.puma.biz.dao.ClientAlarmMetaDao;
import com.dianping.puma.biz.dao.ClientAlarmStrategyDao;
import com.dianping.puma.biz.entity.ClientAlarmBenchmarkEntity;
import com.dianping.puma.biz.entity.ClientAlarmDataEntity;
import com.dianping.puma.biz.entity.ClientAlarmMetaEntity;
import com.dianping.puma.biz.entity.ClientAlarmStrategyEntity;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 * Created by xiaotian.li on 16/3/18.
 * Email: lixiaotian07@gmail.com
 */
public class ClientAlarmServiceImpl implements ClientAlarmService {

    private Converter converter;

    private ClientAlarmDataDao clientAlarmDataDao;

    private ClientAlarmBenchmarkDao clientAlarmBenchmarkDao;

    private ClientAlarmStrategyDao clientAlarmStrategyDao;

    private ClientAlarmMetaDao clientAlarmMetaDao;

    @Override
    public Map<AlarmData, AlarmBenchmark> findDataAndBenchmark(String clientName) {
        ClientAlarmDataEntity dataEntity = clientAlarmDataDao.find(clientName);
        ClientAlarmBenchmarkEntity benchmarkEntity = clientAlarmBenchmarkDao.find(clientName);

        Map<AlarmData, AlarmBenchmark> map = Maps.newHashMap();

        map.put(
                findPullTimeDelayData(dataEntity),
                findPullTimeDelayBenchmark(benchmarkEntity)
        );

        map.put(
                findPushTimeDelayData(dataEntity),
                findPushTimeDelayBenchmark(benchmarkEntity)
        );

        return map;
    }

    private AlarmData findPullTimeDelayData(ClientAlarmDataEntity entity) {
        PullTimeDelayAlarmData data = new PullTimeDelayAlarmData();
        data.setPullTimeDelayInSecond(entity.getPullTimeDelayInSecond());
        return data;
    }

    private AlarmBenchmark findPullTimeDelayBenchmark(ClientAlarmBenchmarkEntity entity) {
        PullTimeDelayAlarmBenchmark benchmark = new PullTimeDelayAlarmBenchmark();
        benchmark.setPullTimeDelayAlarm(entity.isAlarmPullTimeDelay());
        benchmark.setMinPullTimeDelayInSecond(entity.getMinPullTimeDelayInSecond());
        benchmark.setMaxPullTimeDelayInSecond(entity.getMaxPullTimeDelayInSecond());
        return benchmark;
    }

    private AlarmData findPushTimeDelayData(ClientAlarmDataEntity entity) {
        PushTimeDelayAlarmData data = new PushTimeDelayAlarmData();
        data.setPushTimeDelayInSecond(entity.getPushTimeDelayInSecond());
        return data;
    }

    private AlarmBenchmark findPushTimeDelayBenchmark(ClientAlarmBenchmarkEntity entity) {
        PushTimeDelayAlarmBenchmark benchmark = new PushTimeDelayAlarmBenchmark();
        benchmark.setPushTimeDelayAlarm(entity.isAlarmPushTimeDelay());
        benchmark.setMinPushTimeDelayInSecond(entity.getMinPushTimeDelayInSecond());
        benchmark.setMaxPushTimeDelayInSecond(entity.getMaxPushTimeDelayInSecond());
        return benchmark;
    }

    @Override
    public AlarmStrategy findStrategy(String clientName) {
        ClientAlarmStrategyEntity entity = clientAlarmStrategyDao.find(clientName);

        AlarmStrategy strategy = null;

        if (entity.isNoAlarm()) {
            strategy = converter.convert(entity, NoAlarmStrategy.class);
        } else if (entity.isLinearAlarm()) {
            strategy = converter.convert(entity, LinearAlarmStrategy.class);
        } else if (entity.isExponentialAlarm()) {
            strategy = converter.convert(entity, ExponentialAlarmStrategy.class);
        }

        return strategy;
    }

    @Override
    public List<AlarmMeta> findMeta(String clientName) {
        ClientAlarmMetaEntity entity = clientAlarmMetaDao.find(clientName);

        List<AlarmMeta> alarmMetas = Lists.newArrayList();

        if (entity.isAlarmByEmail()) {
            alarmMetas.add(converter.convert(entity, EmailAlarmMeta.class));
        } else if (entity.isAlarmBySms()) {
            alarmMetas.add(converter.convert(entity, SmsAlarmMeta.class));
        } else if (entity.isAlarmByWeChat()) {
            alarmMetas.add(converter.convert(entity, WeChatAlarmMeta.class));
        }

        return alarmMetas;
    }
}
