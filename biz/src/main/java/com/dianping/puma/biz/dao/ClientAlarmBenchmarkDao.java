package com.dianping.puma.biz.dao;

import com.dianping.puma.biz.entity.ClientAlarmBenchmarkEntity;

/**
 * Created by xiaotian.li on 16/3/17.
 * Email: lixiaotian07@gmail.com
 */
public interface ClientAlarmBenchmarkDao {

    ClientAlarmBenchmarkEntity find(String clientName);

    void insert(ClientAlarmBenchmarkEntity entity);

    int update(ClientAlarmBenchmarkEntity entity);

    int updatePullTimeDelay(ClientAlarmBenchmarkEntity entity);

    int updatePushTimeDelay(ClientAlarmBenchmarkEntity entity);

    int delete(String clientName);
}
