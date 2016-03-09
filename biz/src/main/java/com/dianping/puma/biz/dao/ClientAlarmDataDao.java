package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.ClientAlarmDataEntity;

/**
 * Created by xiaotian.li on 16/3/9.
 * Email: lixiaotian07@gmail.com
 */
public interface ClientAlarmDataDao {

    int updatePullTimestamp(ClientAlarmDataEntity entity);

    int updatePushTimestamp(ClientAlarmDataEntity entity);
}
