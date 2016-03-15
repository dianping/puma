package com.dianping.puma.alarm.process;

import com.dianping.puma.alarm.exception.PumaAlarmProcessException;
import com.dianping.puma.alarm.model.data.PumaAlarmData;
import com.dianping.puma.alarm.model.raw.PumaAlarmRawData;
import com.dianping.puma.common.PumaLifeCycle;

/**
 * Created by xiaotian.li on 16/3/15.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaAlarmProcessor extends PumaLifeCycle {

    PumaAlarmData process(PumaAlarmRawData rawData) throws PumaAlarmProcessException;
}
