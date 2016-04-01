package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.ClientAlarmDataEntity;

/**
 * Created by xiaotian.li on 16/3/9.
 * Email: lixiaotian07@gmail.com
 */
public interface ClientAlarmDataDao {

    ClientAlarmDataEntity find(String clientName);

    void insert(ClientAlarmDataEntity entity);

    int update(ClientAlarmDataEntity entity);

    int updatePullTimeDelay(ClientAlarmDataEntity entity);

    int updatePushTimeDelay(ClientAlarmDataEntity entity);

    int delete(String clientName);
}
