package com.dianping.puma.core.model;

import com.google.common.base.Objects;

import java.io.Serializable;

public class BinlogInfo implements Serializable {

    private static final long serialVersionUID = 5056491879587690001L;

    private int serverId;

    private String binlogFile;

    private Long binlogPosition;

    private int eventIndex;

    public BinlogInfo() {
    }

    public BinlogInfo(String binlogFile, Long binlogPosition) {
        this.binlogFile = binlogFile;
        this.binlogPosition = binlogPosition;
        this.eventIndex = 0;
    }

    public BinlogInfo(String binlogFile, Long binlogPosition, int eventIndex) {
        this.binlogFile = binlogFile;
        this.binlogPosition = binlogPosition;
        this.eventIndex = eventIndex;
    }

    public String getBinlogFile() {
        return binlogFile;
    }

    public void setBinlogFile(String binlogFile) {
        this.binlogFile = binlogFile;
    }

    public Long getBinlogPosition() {
        return binlogPosition;
    }

    public void setBinlogPosition(Long binlogPosition) {
        this.binlogPosition = binlogPosition;
    }

    public int getEventIndex() {
        return eventIndex;
    }

    public void setEventIndex(int eventIndex) {
        this.eventIndex = eventIndex;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
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