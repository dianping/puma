package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.ClientAlarmStrategyEntity;

/**
 * Created by xiaotian.li on 16/3/17.
 * Email: lixiaotian07@gmail.com
 */
public interface ClientAlarmStrategyDao {

    ClientAlarmStrategyEntity find(String clientName);

    void insert(ClientAlarmStrategyEntity entity);

    int update(ClientAlarmStrategyEntity entity);

    void delete(String clientName);
}
