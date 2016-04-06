package com.dianping.puma.alarm.core.service;

import com.dianping.puma.alarm.core.model.strategy.ExponentialAlarmStrategy;
import com.dianping.puma.alarm.core.model.strategy.LinearAlarmStrategy;
import com.dianping.puma.alarm.core.model.strategy.NoAlarmStrategy;

import java.util.Map;

/**
 * Created by xiaotian.li on 16/3/20.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaClientAlarmStrategyService {

    NoAlarmStrategy findNo(String clientName);

    LinearAlarmStrategy findLinear(String clientName);

    ExponentialAlarmStrategy findExponential(String clientName);

    Map<String, NoAlarmStrategy> findNoAll();

    Map<String, LinearAlarmStrategy> findLinearAll();

    Map<String, ExponentialAlarmStrategy> findExponentialAll();

    void replaceNo(String clientName, NoAlarmStrategy strategy);

    void replaceLinear(String clientName, LinearAlarmStrategy strategy);

    void replaceExponential(String clientName, ExponentialAlarmStrategy strategy);
}
