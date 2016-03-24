package com.dianping.puma.core.dto.binlog.request;

import io.netty.channel.Channel;

import java.util.concurrent.TimeUnit;

public class BinlogGetRequest extends BinlogRequest {

    private boolean autoAck;

    private int batchSize;

    private long timeout;

    private long startTime;

    private TimeUnit timeUnit;

    private Channel channel;

    private transient String codec;

    public boolean isAutoAck() {
        return autoAck;
    }

    public BinlogGetRequest setAutoAck(boolean autoAck) {
        this.autoAck = autoAck;
        return this;
    }

    public int getBatchSize() {
        if (batchSize <= 0) {
            batchSize = 1;
        }
        return batchSize;
    }

    public BinlogGetRequest setBatchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public long getTimeout() {
        return timeout;
    }

    public BinlogGetRequest setTimeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public BinlogGetRequest setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
        return this;
    }

    public Channel getChannel() {
        return channel;
    }

    public BinlogGetRequest setChannel(Channel channel) {
        this.channel = channel;
        return this;
    }

    public long getStartTime() {
        return startTime;
    }

    public BinlogGetRequest setStartTime(long startTime) {
        this.startTime = startTime;
        return this;
    }

    public boolean isTimeout() {
        return timeUnit != null && timeout > 0 && (startTime + timeUnit.toMillis(timeout) < System.currentTimeMillis());
    }

    public String getCodec() {
        return codec;
    }

    public void setCodec(String codec) {
        this.codec = codec;
    }
}
