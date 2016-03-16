package com.dianping.puma.alarm.notify;

import com.dianping.puma.alarm.exception.PumaAlarmNotifyException;
import com.dianping.puma.alarm.exception.PumaAlarmNotifyUnsupportedException;
import com.dianping.puma.common.AbstractPumaLifeCycle;
import com.dianping.puma.common.model.alarm.meta.AlarmMeta;
import com.dianping.puma.common.model.alarm.result.AlarmResult;

import java.util.List;

/**
 * Created by xiaotian.li on 16/3/16.
 * Email: lixiaotian07@gmail.com
 */
public class FilterAlarmNotifier extends AbstractPumaLifeCycle implements PumaAlarmNotifier {

    private List<PumaAlarmNotifier> notifiers;

    @Override
    public void start() {
        super.start();

        for (PumaAlarmNotifier notifier: notifiers) {
            notifier.start();
        }
    }

    @Override
    public void stop() {
        super.stop();

        for (PumaAlarmNotifier notifier: notifiers) {
            notifier.stop();
        }
    }

    @Override
    public void alarm(AlarmResult result, AlarmMeta meta) throws PumaAlarmNotifyException {
        for (PumaAlarmNotifier notifier: notifiers) {
            try {
                notifier.alarm(result, meta);
            } catch (PumaAlarmNotifyUnsupportedException ignore) {
            }
        }
    }

    public void setNotifiers(List<PumaAlarmNotifier> notifiers) {
        this.notifiers = notifiers;
    }
}
