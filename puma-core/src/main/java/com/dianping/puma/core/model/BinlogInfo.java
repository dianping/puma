package com.dianping.puma.core.model;

import com.google.common.base.Objects;

import java.io.Serializable;

public class BinlogInfo implements Serializable {

    private static final long serialVersionUID = 5056491879587690001L;

    private long serverId;

    private String binlogFile;

    private Long binlogPosition;

    private int eventIndex;

    public BinlogInfo() {
    }

    public BinlogInfo(long serverId, String binlogFile, Long binlogPosition) {
        this(serverId, binlogFile, binlogPosition, 0);
    }

    public BinlogInfo(long serverId, String binlogFile, Long binlogPosition, int eventIndex) {
        this.serverId = serverId;
        this.binlogFile = binlogFile;
        this.binlogPosition = binlogPosition;
        this.eventIndex = eventIndex;
    }

    public String getBinlogFile() {
        return binlogFile;
    }

    public BinlogInfo setBinlogFile(String binlogFile) {
        this.binlogFile = binlogFile;
        return this;
    }

    public Long getBinlogPosition() {
        return binlogPosition;
    }

    public BinlogInfo setBinlogPosition(Long binlogPosition) {
        this.binlogPosition = binlogPosition;
        return this;
    }

    public int getEventIndex() {
        return eventIndex;
    }

    public BinlogInfo setEventIndex(int eventIndex) {
        this.eventIndex = eventIndex;
        return this;
    }

    public long getServerId() {
        return serverId;
    }

    public BinlogInfo setServerId(long serverId) {
        this.serverId = serverId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BinlogInfo that = (BinlogInfo) o;
        return Objects.equal(serverId, that.serverId) &&
                Objects.equal(eventIndex, that.eventIndex) &&
                Objects.equal(binlogFile, that.binlogFile) &&
                Objects.equal(binlogPosition, that.binlogPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(serverId, binlogFile, binlogPosition, eventIndex);
    }

    @Override
    public String toString() {
        return "BinlogInfo{" +
                "serverId=" + serverId +
                ", binlogFile='" + binlogFile + '\'' +
                ", binlogPosition=" + binlogPosition +
                ", eventIndex=" + eventIndex +
                '}';
    }
}