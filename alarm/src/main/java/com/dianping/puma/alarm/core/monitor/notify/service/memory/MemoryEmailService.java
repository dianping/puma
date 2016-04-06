package com.dianping.puma.alarm.core.monitor.notify.service.memory;

import com.dianping.puma.alarm.core.monitor.notify.service.PumaEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xiaotian.li on 16/3/23.
 * Email: lixiaotian07@gmail.com
 */
public class MemoryEmailService implements PumaEmailService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void send(String recipient, String title, String content) {
        logger.info("Send email title[{}] and content[{}] to recipient[{}].",
                title, content, recipient);
    }
}
