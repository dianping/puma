package com.dianping.puma.alarm.deploy;

import com.dianping.puma.common.deploy.PumaController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by xiaotian.li on 16/4/5.
 * Email: lixiaotian07@gmail.com
 */
public class PumaAlarmServerController implements PumaController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String springXml;

    private boolean running = false;

    private ConfigurableApplicationContext context;

    @Override
    public void start() {
        logger.info("Start puma alarm server controller...");

        if (running) {
            logger.warn("Puma alarm server controller has been started.");
        } else {
            context = new ClassPathXmlApplicationContext(springXml);
            running = true;
            logger.info("Success to start puma alarm server controller.");
        }
    }

    @Override
    public void stop() {
        logger.info("Stop puma alarm server controller...");

        if (!running) {
            logger.warn("Puma alarm server controller has been stopped.");
        } else {
            context.close();
            running = false;
            logger.info("Success to stop puma alarm server controller.");
        }
    }

    @Override
    public void restart() {
        stop();
        start();
    }

    public void setSpringXml(String springXml) {
        this.springXml = springXml;
    }
}
