package com.dianping.puma.alarm.core.monitor.filter;

import com.dianping.puma.common.AbstractPumaLifeCycle;

/**
 * Created by xiaotian.li on 16/3/28.
 * Email: lixiaotian07@gmail.com
 */
public abstract class AbstractPumaAlarmFilter extends AbstractPumaLifeCycle implements PumaAlarmFilter {

    protected String generateMnemonic(String namespace, String name, String className) {
        return namespace + "." + name + "." + className;
    }
}
