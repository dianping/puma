package com.dianping.puma.biz.service.remote;

import com.dianping.puma.alarm.core.model.strategy.ExponentialAlarmStrategy;
import com.dianping.puma.alarm.core.model.strategy.LinearAlarmStrategy;
import com.dianping.puma.alarm.core.model.strategy.NoAlarmStrategy;
import com.dianping.puma.alarm.core.service.PumaClientAlarmStrategyService;
import com.dianping.puma.common.convert.Converter;
import com.dianping.puma.biz.dao.ClientAlarmStrategyDao;
import com.dianping.puma.biz.entity.ClientAlarmStrategyEntity;

import java.util.Map;

/**
 * Created by xiaotian.li on 16/3/22.
 * Email: lixiaotian07@gmail.com
 */
public class RemoteClientAlarmStrategyService implements PumaClientAlarmStrategyService {

    private Converter converter;

    private ClientAlarmStrategyDao clientAlarmStrategyDao;

    @Override
    public NoAlarmStrategy findNo(String clientName) {
        ClientAlarmStrategyEntity entity = clientAlarmStrategyDao.find(clientName);
        if (entity == null || !entity.isNoAlarm()) {
            return null;
        } else {
            return converter.convert(entity, NoAlarmStrategy.class);
        }
    }

    @Override
    public LinearAlarmStrategy findLinear(String clientName) {
        ClientAlarmStrategyEntity entity = clientAlarmStrategyDao.find(clientName);
        if (entity == null || !entity.isLinearAlarm()) {
            return null;
        } else {
            return converter.convert(entity, LinearAlarmStrategy.class);
        }
    }

    @Override
    public ExponentialAlarmStrategy findExponential(String clientName) {
        ClientAlarmStrategyEntity entity = clientAlarmStrategyDao.find(clientName);
        if (entity == null || !entity.isExponentialAlarm()) {
            return null;
        } else {
            return converter.convert(entity, ExponentialAlarmStrategy.class);
        }
    }

    @Override
    public Map<String, NoAlarmStrategy> findNoAll() {
        return null;
    }

    @Override
    public Map<String, LinearAlarmStrategy> findLinearAll() {
        return null;
    }

    @Override
    public Map<String, ExponentialAlarmStrategy> findExponentialAll() {
        return null;
    }

    @Override
    public void replaceNo(String clientName, NoAlarmStrategy strategy) {

    }

    @Override
    public void replaceLinear(String clientName, LinearAlarmStrategy strategy) {

    }

    @Override
    public void replaceExponential(String clientName, ExponentialAlarmStrategy strategy) {

    }

    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    public void setClientAlarmStrategyDao(ClientAlarmStrategyDao clientAlarmStrategyDao) {
        this.clientAlarmStrategyDao = clientAlarmStrategyDao;
    }
}
