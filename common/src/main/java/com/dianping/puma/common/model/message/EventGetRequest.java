package com.dianping.puma.common.model.message;

import java.util.concurrent.TimeUnit;

/**
 * Created by xiaotian.li on 16/3/12.
 * Email: lixiaotian07@gmail.com
 */
public class EventGetRequest extends EventRequest {

    private int batchSize;

    private long timeout;

    private TimeUnit timeUnit;

    private boolean autoAck;

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public boolean isAutoAck() {
        return autoAck;
    }

    public void setAutoAck(boolean autoAck) {
        this.autoAck = autoAck;
    }
}
