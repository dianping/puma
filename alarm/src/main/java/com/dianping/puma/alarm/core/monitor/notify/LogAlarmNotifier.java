package com.dianping.puma.alarm.core.monitor.notify;

import com.dianping.puma.alarm.core.model.AlarmResult;
import com.dianping.puma.alarm.core.model.meta.AlarmMeta;
import com.dianping.puma.alarm.core.model.meta.LogAlarmMeta;
import com.dianping.puma.alarm.exception.PumaAlarmNotifyException;
import com.dianping.puma.alarm.exception.PumaAlarmNotifyUnsupportedException;
import com.dianping.puma.common.AbstractPumaLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xiaotian.li on 16/3/20.
 * Email: lixiaotian07@gmail.com
 */
public class LogAlarmNotifier extends AbstractPumaLifeCycle implements PumaAlarmNotifier {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void notify(AlarmResult result, AlarmMeta meta) throws PumaAlarmNotifyException {
        if (!(meta instanceof LogAlarmMeta)) {
            throw new PumaAlarmNotifyUnsupportedException("unsupported alarm meta[%s]", meta);
        }

        if (!result.isAlarm()) {
            return;
        }

        logger.info(result.getTitle() + result.getContent());
    }
}
