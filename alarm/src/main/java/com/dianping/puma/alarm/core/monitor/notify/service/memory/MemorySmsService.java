package com.dianping.puma.alarm.core.monitor.notify.service.memory;

import com.dianping.puma.alarm.core.monitor.notify.service.PumaSmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xiaotian.li on 16/3/23.
 * Email: lixiaotian07@gmail.com
 */
public class MemorySmsService implements PumaSmsService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void send(String recipient, String message) {
        logger.info("Send sms message[{}] to recipient[{}].", recipient, message);
    }
}
