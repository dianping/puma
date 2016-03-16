package com.dianping.puma.alarm.notify;

import com.dianping.puma.alarm.exception.PumaAlarmNotifyException;
import com.dianping.puma.common.PumaLifeCycle;
import com.dianping.puma.common.model.alarm.meta.AlarmMeta;
import com.dianping.puma.common.model.alarm.result.AlarmResult;

/**
 * Created by xiaotian.li on 16/3/8.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaAlarmNotifier extends PumaLifeCycle {

    void alarm(AlarmResult result, AlarmMeta meta) throws PumaAlarmNotifyException;
}
