package com.dianping.puma.alarm.core.service.memory;

import com.dianping.puma.alarm.core.model.strategy.ExponentialAlarmStrategy;
import com.dianping.puma.alarm.core.model.strategy.LinearAlarmStrategy;
import com.dianping.puma.alarm.core.model.strategy.NoAlarmStrategy;
import com.dianping.puma.alarm.core.service.PumaClientAlarmStrategyService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapMaker;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by xiaotian.li on 16/3/20.
 * Email: lixiaotian07@gmail.com
 */
public class MemoryClientAlarmStrategyService implements PumaClientAlarmStrategyService {

    private ConcurrentMap<String, NoAlarmStrategy> noAlarmStrategyMap
            = new MapMaker().makeMap();

    private ConcurrentMap<String, LinearAlarmStrategy> linearAlarmStrategyMap
            = new MapMaker().makeMap();

    private ConcurrentMap<String, ExponentialAlarmStrategy> exponentialAlarmStrategyMap
            = new MapMaker().makeMap();

    @Override
    public NoAlarmStrategy findNo(String clientName) {
        return noAlarmStrategyMap.get(clientName);
    }

    @Override
    public LinearAlarmStrategy findLinear(String clientName) {
        return linearAlarmStrategyMap.get(clientName);
    }

    @Override
    public ExponentialAlarmStrategy findExponential(String clientName) {
        return exponentialAlarmStrategyMap.get(clientName);
    }

    @Override
    public Map<String, NoAlarmStrategy> findNoAll() {
        return ImmutableMap.copyOf(noAlarmStrategyMap);
    }

    @Override
    public Map<String, LinearAlarmStrategy> findLinearAll() {
        return ImmutableMap.copyOf(linearAlarmStrategyMap);
    }

    @Override
    public Map<String, ExponentialAlarmStrategy> findExponentialAll() {
        return ImmutableMap.copyOf(exponentialAlarmStrategyMap);
    }

    @Override
    public void replaceNo(String clientName, NoAlarmStrategy strategy) {
        noAlarmStrategyMap.put(clientName, strategy);
    }

    @Override
    public void replaceLinear(String clientName, LinearAlarmStrategy strategy) {
        linearAlarmStrategyMap.put(clientName, strategy);
    }

    @Override
    public void replaceExponential(String clientName, ExponentialAlarmStrategy strategy) {
        exponentialAlarmStrategyMap.put(clientName, strategy);
    }
}
