package com.dianping.puma.biz.service.remote;

import com.dianping.puma.alarm.ha.model.AlarmServerHeartbeat;
import com.dianping.puma.alarm.ha.service.PumaAlarmServerHeartbeatService;
import com.dianping.puma.common.convert.Converter;
import com.dianping.puma.biz.dao.AlarmServerHeartbeatDao;
import com.dianping.puma.biz.entity.AlarmServerHeartbeatEntity;
import com.dianping.puma.common.exception.PumaServiceException;
import com.google.gson.reflect.TypeToken;

import java.util.List;

/**
 * Created by xiaotian.li on 16/4/5.
 * Email: lixiaotian07@gmail.com
 */
public class RemoteAlarmServerHeartbeatService implements PumaAlarmServerHeartbeatService {

    private Converter converter;

    private AlarmServerHeartbeatDao alarmServerHeartbeatDao;

    @Override
    public List<AlarmServerHeartbeat> findAll() throws PumaServiceException {
        List<AlarmServerHeartbeatEntity> entities = alarmServerHeartbeatDao.findAll();
        return converter.convert(entities, new TypeToken<List<AlarmServerHeartbeat>>(){}.getType());
    }

    @Override
    public AlarmServerHeartbeat findHeartbeat(String host) throws PumaServiceException {
        AlarmServerHeartbeatEntity entity = alarmServerHeartbeatDao.find(host);
        return converter.convert(entity, AlarmServerHeartbeat.class);
    }

    @Override
    public void heartbeat(AlarmServerHeartbeat heartbeat) throws PumaServiceException {
        AlarmServerHeartbeatEntity entity = converter.convert(heartbeat, AlarmServerHeartbeatEntity.class);
        int result = alarmServerHeartbeatDao.update(entity);
        if (result == 0) {
            try {
                alarmServerHeartbeatDao.insert(entity);
            } catch (Throwable t) {
                alarmServerHeartbeatDao.update(entity);
            }
        }
    }

    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    public void setAlarmServerHeartbeatDao(AlarmServerHeartbeatDao alarmServerHeartbeatDao) {
        this.alarmServerHeartbeatDao = alarmServerHeartbeatDao;
    }
}
