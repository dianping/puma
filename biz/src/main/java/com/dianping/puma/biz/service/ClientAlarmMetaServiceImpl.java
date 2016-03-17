package com.dianping.puma.biz.service;

import com.dianping.puma.alarm.service.ClientAlarmMetaService;
import com.dianping.puma.biz.convert.Converter;
import com.dianping.puma.biz.dao.ClientAlarmMetaDao;
import com.dianping.puma.biz.entity.ClientAlarmMetaEntity;
import com.dianping.puma.common.model.alarm.meta.AlarmMeta;

/**
 * Created by xiaotian.li on 16/3/17.
 * Email: lixiaotian07@gmail.com
 */
public class ClientAlarmMetaServiceImpl implements ClientAlarmMetaService {

    private Converter converter;

    private ClientAlarmMetaDao clientAlarmMetaDao;

    @Override
    public AlarmMeta find(String clientName) {
        ClientAlarmMetaEntity entity = clientAlarmMetaDao.find(clientName);
        return converter.convert(entity, AlarmMeta.class);
    }

    @Override
    public void create(AlarmMeta alarmMeta) {
        ClientAlarmMetaEntity entity = converter.convert(alarmMeta, ClientAlarmMetaEntity.class);
        clientAlarmMetaDao.insert(entity);
    }

    @Override
    public int update(AlarmMeta alarmMeta) {
        ClientAlarmMetaEntity entity = converter.convert(alarmMeta, ClientAlarmMetaEntity.class);
        return clientAlarmMetaDao.update(entity);
    }

    @Override
    public void remove(String clientName) {
        clientAlarmMetaDao.delete(clientName);
    }
}
