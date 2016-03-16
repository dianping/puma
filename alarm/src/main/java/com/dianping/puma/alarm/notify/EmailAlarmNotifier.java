package com.dianping.puma.alarm.notify;

import com.dianping.puma.alarm.exception.PumaAlarmNotifyException;
import com.dianping.puma.alarm.exception.PumaAlarmNotifyUnsupportedException;
import com.dianping.puma.alarm.service.EmailService;
import com.dianping.puma.common.AbstractPumaLifeCycle;
import com.dianping.puma.common.model.alarm.meta.AlarmMeta;
import com.dianping.puma.common.model.alarm.result.AlarmResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by xiaotian.li on 16/3/8.
 * Email: lixiaotian07@gmail.com
 */
public class EmailAlarmNotifier extends AbstractPumaLifeCycle implements PumaAlarmNotifier {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private EmailService emailService;

    @Override
    public void alarm(AlarmResult result, AlarmMeta meta) throws PumaAlarmNotifyException {
        if (meta.isAlarmByEmail()) {
            throw new PumaAlarmNotifyUnsupportedException("unsupported alarm meta[%s]", meta);
        }

        if (!result.isAlarm()) {
            return;
        }

        List<String> emails = meta.getEmails();
        if (emails != null) {
            for (String email: emails) {
                try {
                    emailService.send(email, result.getTitle(), result.getHead() + result.getBody());
                } catch (Throwable t) {
                    logger.error("Failed to send email to destination[{}].", email);
                }
            }
        }
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }
}
