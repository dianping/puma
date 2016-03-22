package com.dianping.puma.server.intercept;

import com.dianping.puma.alarm.log.PumaAlarmLogger;
import com.dianping.puma.common.AbstractPumaLifeCycle;
import com.dianping.puma.common.intercept.PumaInterceptor;
import com.dianping.puma.common.intercept.exception.PumaInterceptException;
import com.dianping.puma.common.utils.Clock;
import com.dianping.puma.core.dto.BinlogHttpMessage;
import com.dianping.puma.core.dto.BinlogMessage;
import com.dianping.puma.core.dto.binlog.response.BinlogGetResponse;
import com.dianping.puma.core.model.BinlogInfo;

/**
 * Created by xiaotian.li on 16/3/22.
 * Email: lixiaotian07@gmail.com
 */
public class PushTimeDelayAlarmInterceptor extends AbstractPumaLifeCycle
        implements PumaInterceptor<BinlogHttpMessage> {

    private PumaAlarmLogger pumaAlarmLogger;

    private Clock clock = new Clock();

    @Override
    public void before(BinlogHttpMessage data) throws PumaInterceptException {
        // do nothing.
    }

    @Override
    public void after(BinlogHttpMessage data) throws PumaInterceptException {
        if (data instanceof BinlogGetResponse) {
            BinlogGetResponse response = (BinlogGetResponse) data;

            String clientName = response.getClientName();
            BinlogMessage binlogMessage = response.getBinlogMessage();
            BinlogInfo binlogInfo = binlogMessage.getLastBinlogInfo();
            if (binlogInfo != null) {
                long pushTime = binlogInfo.getTimestamp();
                pumaAlarmLogger.logPushTime(clientName, pushTime);
            }
        }
    }

    @Override
    public void error(BinlogHttpMessage data) throws PumaInterceptException {
        // do nothing.
    }

    public void setPumaAlarmLogger(PumaAlarmLogger pumaAlarmLogger) {
        this.pumaAlarmLogger = pumaAlarmLogger;
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }
}
