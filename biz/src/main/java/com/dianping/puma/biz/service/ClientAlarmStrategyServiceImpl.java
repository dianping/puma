package com.dianping.puma.biz.service;

import com.dianping.puma.alarm.service.ClientAlarmStrategyService;
import com.dianping.puma.biz.convert.Converter;
import com.dianping.puma.biz.dao.ClientAlarmStrategyDao;
import com.dianping.puma.biz.entity.ClientAlarmStrategyEntity;
import com.dianping.puma.common.model.alarm.strategy.AlarmStrategy;

/**
 * Created by xiaotian.li on 16/3/17.
 * Email: lixiaotian07@gmail.com
 */
public class ClientAlarmStrategyServiceImpl implements ClientAlarmStrategyService {

    private Converter converter;

    private ClientAlarmStrategyDao clientAlarmStrategyDao;

    @Override
    public AlarmStrategy find(String clientName) {
        ClientAlarmStrategyEntity entity = clientAlarmStrategyDao.find(clientName);
        return converter.convert(entity, AlarmStrategy.class);
    }

    @Override
    public void create(AlarmStrategy strategy) {
        ClientAlarmStrategyEntity entity = converter.convert(strategy, ClientAlarmStrategyEntity.class);
        clientAlarmStrategyDao.insert(entity);
    }

    @Override
    public int update(AlarmStrategy strategy) {
        ClientAlarmStrategyEntity entity = converter.convert(strategy, ClientAlarmStrategyEntity.class);
        return clientAlarmStrategyDao.update(entity);
    }

    @Override
    public void remove(String clientName) {
        clientAlarmStrategyDao.delete(clientName);
    }
}
