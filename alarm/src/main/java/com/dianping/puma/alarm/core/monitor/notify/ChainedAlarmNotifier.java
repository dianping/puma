package com.dianping.puma.alarm.core.monitor.notify;

import com.dianping.puma.alarm.core.model.AlarmResult;
import com.dianping.puma.alarm.core.model.meta.AlarmMeta;
import com.dianping.puma.alarm.exception.PumaAlarmNotifyException;
import com.dianping.puma.alarm.exception.PumaAlarmNotifyUnsupportedException;
import com.dianping.puma.common.AbstractPumaLifeCycle;

import java.util.List;

/**
 * Created by xiaotian.li on 16/3/16.
 * Email: lixiaotian07@gmail.com
 */
public class ChainedAlarmNotifier extends AbstractPumaLifeCycle implements PumaAlarmNotifier {

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
    public void notify(AlarmResult result, AlarmMeta meta) throws PumaAlarmNotifyException {
        for (PumaAlarmNotifier notifier: notifiers) {
            try {
                notifier.notify(result, meta);
            } catch (PumaAlarmNotifyUnsupportedException ignore) {
            }
        }
    }

    public void setNotifiers(List<PumaAlarmNotifier> notifiers) {
        this.notifiers = notifiers;
    }
}
