package com.dianping.puma.alarm.service;

import com.dianping.puma.common.model.alarm.strategy.AlarmStrategy;

/**
 * Created by xiaotian.li on 16/3/16.
 * Email: lixiaotian07@gmail.com
 */
public interface ClientAlarmStrategyService {

    AlarmStrategy find(String clientName);

    void create(AlarmStrategy strategy);

    int update(AlarmStrategy strategy);

    void remove(String clientName);
}
