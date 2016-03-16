package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.convert.Converter;
import com.dianping.puma.biz.dao.ClientAlarmDataDao;
import com.dianping.puma.biz.entity.ClientAlarmDataEntity;
import com.dianping.puma.biz.service.ClientAlarmDataService;
import com.dianping.puma.common.model.ClientAlarmData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by xiaotian.li on 16/3/9.
 * Email: lixiaotian07@gmail.com
 */
@Service
public class ClientAlarmDataServiceImpl implements ClientAlarmDataService {

    @Autowired
    Converter converter;

    @Autowired
    ClientAlarmDataDao clientAlarmDataDao;

    @Override
    public List<ClientAlarmData> findAll() {
        return null;
    }

    @Override
    public void create(String clientName, ClientAlarmData clientAlarmData) {
        ClientAlarmDataEntity entity = converter.convert(clientAlarmData, ClientAlarmDataEntity.class);
        entity.setClientName(clientName);
        clientAlarmDataDao.insert(entity);
    }

    @Override
    public int updatePullTime(String clientName, ClientAlarmData clientAlarmData) {
        ClientAlarmDataEntity entity = converter.convert(clientAlarmData, ClientAlarmDataEntity.class);
        entity.setClientName(clientName);
        return clientAlarmDataDao.updatePullTime(entity);
    }

    @Override
    public int updatePushTime(String clientName, ClientAlarmData clientAlarmData) {
        ClientAlarmDataEntity entity = converter.convert(clientAlarmData, ClientAlarmDataEntity.class);
        entity.setClientName(clientName);
        return clientAlarmDataDao.updatePushTime(entity);
    }

    @Override
    public void replacePullTime(String clientName, ClientAlarmData clientAlarmData) {
        int result = updatePullTime(clientName, clientAlarmData);
        if (result == 0) {
            try {
                create(clientName, clientAlarmData);
            } catch (Throwable ignore) {
                updatePullTime(clientName, clientAlarmData);
            }
        }
    }

    @Override
    public void replacePushTime(String clientName, ClientAlarmData clientAlarmData) {
        int result = updatePushTime(clientName, clientAlarmData);
        if (result == 0) {
            try {
                create(clientName, clientAlarmData);
            } catch (Throwable ignore) {
                updatePushTime(clientName, clientAlarmData);
            }
        }
    }
}
