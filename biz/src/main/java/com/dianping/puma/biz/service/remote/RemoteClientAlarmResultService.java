package com.dianping.puma.biz.service.remote;

import com.dianping.puma.alarm.core.model.AlarmResult;
import com.dianping.puma.alarm.core.service.PumaClientAlarmResultService;
import com.dianping.puma.biz.dao.ClientAlarmResultDao;
import com.dianping.puma.biz.entity.ClientAlarmResultEntity;
import com.dianping.puma.common.convert.Converter;
import com.dianping.puma.common.exception.PumaServiceException;

/**
 * Created by xiaotian.li on 16/4/8.
 * Email: lixiaotian07@gmail.com
 */
public class RemoteClientAlarmResultService implements PumaClientAlarmResultService {

    private Converter converter;

    private ClientAlarmResultDao clientAlarmResultDao;

    @Override
    public AlarmResult find(String clientName) throws PumaServiceException {
        try {
            ClientAlarmResultEntity entity = clientAlarmResultDao.find(clientName);
            return converter.convert(entity, AlarmResult.class);
        } catch (Throwable t) {
            throw new PumaServiceException(t);
        }
    }

    @Override
    public void create(String clientName, AlarmResult alarmResult) throws PumaServiceException {
        try {
            ClientAlarmResultEntity entity = converter.convert(alarmResult, ClientAlarmResultEntity.class);
            entity.setClientName(clientName);
            clientAlarmResultDao.insert(entity);
        } catch (Throwable t) {
            throw new PumaServiceException(t);
        }
    }

    @Override
    public int update(String clientName, AlarmResult alarmResult) throws PumaServiceException {
        try {
            ClientAlarmResultEntity entity = converter.convert(alarmResult, ClientAlarmResultEntity.class);
            entity.setClientName(clientName);
            return clientAlarmResultDao.update(entity);
        } catch (Throwable t) {
            throw new PumaServiceException(t);
        }
    }

    @Override
    public void delete(String clientName) throws PumaServiceException {
        try {
            clientAlarmResultDao.delete(clientName);
        } catch (Throwable t) {
            throw new PumaServiceException(t);
        }
    }

    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    public void setClientAlarmResultDao(ClientAlarmResultDao clientAlarmResultDao) {
        this.clientAlarmResultDao = clientAlarmResultDao;
    }
}
