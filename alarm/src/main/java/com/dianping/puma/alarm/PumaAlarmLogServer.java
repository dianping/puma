package com.dianping.puma.alarm;

import com.dianping.puma.alarm.log.PumaAlarmLogger;
import com.dianping.puma.common.AbstractPumaLifeCycle;
import com.dianping.puma.common.server.PumaServer;

/**
 * Created by xiaotian.li on 16/3/25.
 * Email: lixiaotian07@gmail.com
 */
public class PumaAlarmLogServer extends AbstractPumaLifeCycle implements PumaServer {

    private PumaAlarmLogger logger;

    @Override
    public void start() {
        super.start();

        logger.start();
    }

    @Override
    public void stop() {
        super.stop();

        logger.stop();
    }

    public void setLogger(PumaAlarmLogger logger) {
        this.logger = logger;
    }
}
