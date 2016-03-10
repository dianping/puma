package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.ClientAlarmDataEntity;

/**
 * Created by xiaotian.li on 16/3/9.
 * Email: lixiaotian07@gmail.com
 */
public interface ClientAlarmDataDao {

    void insert(ClientAlarmDataEntity entity);

    int updatePullTime(ClientAlarmDataEntity entity);

    int updatePushTime(ClientAlarmDataEntity entity);

    void delete(String clientName);
}
