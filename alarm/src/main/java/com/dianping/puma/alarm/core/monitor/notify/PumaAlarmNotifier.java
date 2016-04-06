package com.dianping.puma.alarm.core.monitor.notify;

import com.dianping.puma.alarm.core.model.AlarmResult;
import com.dianping.puma.alarm.core.model.meta.AlarmMeta;
import com.dianping.puma.alarm.exception.PumaAlarmNotifyException;
import com.dianping.puma.common.PumaLifeCycle;

/**
 * Created by xiaotian.li on 16/3/8.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaAlarmNotifier extends PumaLifeCycle {

    void notify(AlarmResult result, AlarmMeta meta) throws PumaAlarmNotifyException;
}
