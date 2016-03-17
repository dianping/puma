package com.dianping.puma.alarm.notify;

import com.dianping.puma.alarm.exception.PumaAlarmNotifyException;
import com.dianping.puma.common.AbstractPumaLifeCycle;
import com.dianping.puma.alarm.model.meta.AlarmMeta;
import com.dianping.puma.alarm.model.result.AlarmResult;

/**
 * Created by xiaotian.li on 16/3/17.
 * Email: lixiaotian07@gmail.com
 */
public class WeChatAlarmNotifier extends AbstractPumaLifeCycle implements PumaAlarmNotifier {

    @Override
    public void notify(AlarmResult result, AlarmMeta meta) throws PumaAlarmNotifyException {

    }
}
