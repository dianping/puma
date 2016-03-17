package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.ClientAlarmBenchmarkEntity;

/**
 * Created by xiaotian.li on 16/3/17.
 * Email: lixiaotian07@gmail.com
 */
public interface ClientAlarmBenchmarkDao {

    ClientAlarmBenchmarkEntity find(String clientName);

    void insert(ClientAlarmBenchmarkEntity entity);

    int updatePullTimeDelay(ClientAlarmBenchmarkEntity entity);

    int updatePushTimeDelay(ClientAlarmBenchmarkEntity entity);

    void delete(String clientName);
}
