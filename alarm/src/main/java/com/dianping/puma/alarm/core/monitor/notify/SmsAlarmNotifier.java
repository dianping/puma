package com.dianping.puma.alarm.core.monitor.notify;

import com.dianping.puma.alarm.core.model.AlarmResult;
import com.dianping.puma.alarm.core.model.meta.AlarmMeta;
import com.dianping.puma.alarm.core.model.meta.SmsAlarmMeta;
import com.dianping.puma.alarm.core.monitor.notify.service.PumaSmsService;
import com.dianping.puma.alarm.exception.PumaAlarmNotifyException;
import com.dianping.puma.alarm.exception.PumaAlarmNotifyUnsupportedException;
import com.dianping.puma.common.AbstractPumaLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by xiaotian.li on 16/3/17.
 * Email: lixiaotian07@gmail.com
 */
public class SmsAlarmNotifier extends AbstractPumaLifeCycle implements PumaAlarmNotifier {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private PumaSmsService smsService;

    @Override
    public void notify(AlarmResult result, AlarmMeta meta) throws PumaAlarmNotifyException {
        if (!(meta instanceof SmsAlarmMeta)) {
            throw new PumaAlarmNotifyUnsupportedException("unsupported alarm meta[%s]", meta);
        }

        if (!result.isAlarm()) {
            return;
        }

        String title = result.getTitle();
        String content = result.getContent();

        SmsAlarmMeta smsAlarmMeta = (SmsAlarmMeta) meta;
        List<String> recipients = smsAlarmMeta.getSmsRecipients();
        for (String recipient: recipients) {
            try {
                smsService.send(recipient, title + content);
            } catch (Throwable t) {
                logger.error("Failed to send sms to recipient[{}].", recipient, t);
            }
        }
    }

    public void setSmsService(PumaSmsService smsService) {
        this.smsService = smsService;
    }
}
