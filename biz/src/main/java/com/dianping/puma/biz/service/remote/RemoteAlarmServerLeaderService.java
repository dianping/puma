package com.dianping.puma.biz.service.remote;

import com.dianping.puma.alarm.ha.model.AlarmServerLeader;
import com.dianping.puma.alarm.ha.service.PumaAlarmServerLeaderService;
import com.dianping.puma.common.convert.Converter;
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
        AlarmServerLeaderEntity entity = alarmServerLeaderDao.find();
        return converter.convert(entity, AlarmServerLeader.class);
    }

    @Override
    public boolean takeLeader(AlarmServerLeader leader) throws PumaServiceException {
        AlarmServerLeaderEntity oriEntity = alarmServerLeaderDao.find();
        AlarmServerLeaderEntity entity = converter.convert(leader, AlarmServerLeaderEntity.class);

        if (oriEntity == null) {
            entity.setVersion(0);
            try {
                alarmServerLeaderDao.insert(entity);
                return true;
            } catch (Throwable ignore) {
                return false;
            }
        } else {
            long oriVersion = oriEntity.getVersion();
            entity.setVersion(oriVersion + 1);
            int result = alarmServerLeaderDao.update(oriVersion, entity);
            return result == 1;
        }
    }

    @Override
    public void releaseLeader(String host) throws PumaServiceException {
        alarmServerLeaderDao.delete(host);
    }

    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    public void setAlarmServerLeaderDao(AlarmServerLeaderDao alarmServerLeaderDao) {
        this.alarmServerLeaderDao = alarmServerLeaderDao;
    }
}
