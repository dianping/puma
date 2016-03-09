package com.dianping.puma.biz.service.impl;

import com.dianping.puma.biz.dao.ClientAlarmDataDao;
import com.dianping.puma.biz.entity.ClientAlarmDataEntity;
import com.dianping.puma.biz.service.ClientAlarmDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by xiaotian.li on 16/3/9.
 * Email: lixiaotian07@gmail.com
 */
@Service
public class ClientAlarmDataServiceImpl implements ClientAlarmDataService {

    @Autowired
    ClientAlarmDataDao clientAlarmDataDao;

    @Override
    public int updatePullTimestamp(String clientName, Long pullTimestamp) {
        ClientAlarmDataEntity entity = new ClientAlarmDataEntity();
        entity.setClientName(clientName);
        entity.setPullTimestamp(pullTimestamp);
        return clientAlarmDataDao.updatePullTimestamp(entity);
    }

    @Override
    public int updatePushTimestamp(String clientName, Long pushTimestamp) {
        ClientAlarmDataEntity entity = new ClientAlarmDataEntity();
        entity.setClientName(clientName);
        entity.setPushTimestamp(pushTimestamp);
        return clientAlarmDataDao.updatePushTimestamp(entity);
    }
}
