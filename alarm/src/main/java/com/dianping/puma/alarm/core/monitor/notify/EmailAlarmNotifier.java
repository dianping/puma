package com.dianping.puma.alarm.core.monitor.notify;

import com.dianping.puma.alarm.core.model.AlarmResult;
import com.dianping.puma.alarm.core.model.meta.AlarmMeta;
import com.dianping.puma.alarm.core.model.meta.EmailAlarmMeta;
import com.dianping.puma.alarm.core.monitor.notify.service.PumaEmailService;
import com.dianping.puma.alarm.exception.PumaAlarmNotifyException;
import com.dianping.puma.alarm.exception.PumaAlarmNotifyUnsupportedException;
import com.dianping.puma.common.AbstractPumaLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by xiaotian.li on 16/3/8.
 * Email: lixiaotian07@gmail.com
 */
public class EmailAlarmNotifier extends AbstractPumaLifeCycle implements PumaAlarmNotifier {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private PumaEmailService emailService;

    @Override
    public void notify(AlarmResult result, AlarmMeta meta) throws PumaAlarmNotifyException {
        if (!(meta instanceof EmailAlarmMeta)) {
            throw new PumaAlarmNotifyUnsupportedException("unsupported alarm meta[%s]", meta);
        }

        if (!result.isAlarm()) {
            return;
        }

        String title = result.getTitle();
        String content = result.getContent();

        EmailAlarmMeta emailAlarmMeta = (EmailAlarmMeta) meta;
        List<String> recipients = emailAlarmMeta.getEmailRecipients();

        if (recipients != null) {
            for (String recipient: recipients) {
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Send email with title[{}] and content[{}] " +
                                "to recipient[{}] .", title, content, recipient);
                    }

                    emailService.send(recipient, title, content);
                } catch (Throwable t) {
                    logger.error("Failed to send email with title[{}] and content[{}] " +
                            "to recipient[{}].", title, content, recipient, t);
                }
            }
        }
    }

    public void setEmailService(PumaEmailService emailService) {
        this.emailService = emailService;
    }
}
