package com.dianping.puma.biz.service;

import com.dianping.puma.alarm.service.ClientAlarmDataService;
import com.dianping.puma.biz.convert.Converter;
import com.dianping.puma.biz.dao.ClientAlarmDataDao;
import com.dianping.puma.biz.entity.ClientAlarmDataEntity;
import com.dianping.puma.common.model.alarm.data.AlarmData;
import com.dianping.puma.common.model.alarm.data.PullTimeDelayAlarmData;
import com.dianping.puma.common.model.alarm.data.PushTimeDelayAlarmData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public AlarmData findPullTimeDelay(String clientName) {
        ClientAlarmDataEntity entity = clientAlarmDataDao.find(clientName);
        return converter.convert(entity, PullTimeDelayAlarmData.class);
    }

    @Override
    public AlarmData findPushTimeDelay(String clientName) {
        ClientAlarmDataEntity entity = clientAlarmDataDao.find(clientName);
        return converter.convert(entity, PushTimeDelayAlarmData.class);
    }

    @Override
    public void createPullTimeDelay(String clientName, PullTimeDelayAlarmData data) {
        ClientAlarmDataEntity entity = converter.convert(data, ClientAlarmDataEntity.class);
        entity.setClientName(clientName);
        clientAlarmDataDao.insert(entity);
    }

    @Override
    public void createPushTimeDelay(String clientName, PushTimeDelayAlarmData data) {
        ClientAlarmDataEntity entity = converter.convert(data, ClientAlarmDataEntity.class);
        entity.setClientName(clientName);
        clientAlarmDataDao.insert(entity);
    }

    @Override
    public int updatePullTimeDelay(String clientName, PullTimeDelayAlarmData data) {
        ClientAlarmDataEntity entity = converter.convert(data, ClientAlarmDataEntity.class);
        entity.setClientName(clientName);
        return clientAlarmDataDao.updatePullTimeDelay(entity);
    }

    @Override
    public int updatePushTimeDelay(String clientName, PushTimeDelayAlarmData data) {
        ClientAlarmDataEntity entity = converter.convert(data, ClientAlarmDataEntity.class);
        entity.setClientName(clientName);
        return clientAlarmDataDao.updatePushTimeDelay(entity);
    }

    @Override
    public void replacePullTimeDelay(String clientName, PullTimeDelayAlarmData data) {
        int result = updatePullTimeDelay(clientName, data);
        if (result == 0) {
            try {
                createPullTimeDelay(clientName, data);
            } catch (Throwable ignore) {
                updatePullTimeDelay(clientName, data);
            }
        }
    }

    @Override
    public void replacePushTimeDelay(String clientName, PushTimeDelayAlarmData data) {
        int result = updatePushTimeDelay(clientName, data);
        if (result == 0) {
            try {
                createPushTimeDelay(clientName, data);
            } catch (Throwable ignore) {
                updatePushTimeDelay(clientName, data);
            }
        }
    }
}
