package com.dianping.puma.biz.service.remote;

import com.dianping.puma.alarm.ha.model.AlarmServerLeader;
import com.dianping.puma.alarm.ha.service.PumaAlarmServerLeaderService;
import com.dianping.puma.biz.convert.Converter;
import com.dianping.puma.biz.dao.AlarmServerLeaderDao;
import com.dianping.puma.biz.entity.AlarmServerLeaderEntity;
import com.dianping.puma.common.exception.PumaServiceException;

/**
 * Created by xiaotian.li on 16/4/5.
 * Email: lixiaotian07@gmail.com
 */
public class RemoteAlarmServerLeaderService implements PumaAlarmServerLeaderService {

    private Converter converter;

    private AlarmServerLeaderDao alarmServerLeaderDao;

    @Override
    public AlarmServerLeader findLeader() throws PumaServiceException {
        AlarmServerLeaderEntity entity = alarmServerLeaderDao.find(true);
        return converter.convert(entity, AlarmServerLeader.class);
    }

    @Override
    public void takeLeader(AlarmServerLeader leader) throws PumaServiceException {
        AlarmServerLeaderEntity entity = converter.convert(leader, AlarmServerLeaderEntity.class);
        entity.setLeader(true);

        int result = alarmServerLeaderDao.update(entity);
        if (result == 0) {
            try {
                alarmServerLeaderDao.insert(entity);
            } catch (Throwable t) {
                alarmServerLeaderDao.update(entity);
            }
        }
    }

    @Override
    public void releaseLeader(AlarmServerLeader leader) throws PumaServiceException {
        AlarmServerLeaderEntity entity = converter.convert(leader, AlarmServerLeaderEntity.class);
        entity.setLeader(true);
        alarmServerLeaderDao.delete(entity);
    }

    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    public void setAlarmServerLeaderDao(AlarmServerLeaderDao alarmServerLeaderDao) {
        this.alarmServerLeaderDao = alarmServerLeaderDao;
    }
}
