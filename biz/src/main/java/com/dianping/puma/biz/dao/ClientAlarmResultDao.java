package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.ClientAlarmResultEntity;

/**
 * Created by xiaotian.li on 16/4/8.
 * Email: lixiaotian07@gmail.com
 */
public interface ClientAlarmResultDao {

    ClientAlarmResultEntity find(String clientName);

    int insert(ClientAlarmResultEntity entity);

    int update(ClientAlarmResultEntity entity);

    int delete(String clientName);
}
